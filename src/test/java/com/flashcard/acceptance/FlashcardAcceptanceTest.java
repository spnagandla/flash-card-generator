package com.flashcard.acceptance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashcard.controller.FlashcardController;
import com.flashcard.model.Flashcard;
import com.flashcard.model.FlashcardJob;
import com.flashcard.model.FlashcardResultResponse;
import com.flashcard.model.JobStatusResponse;
import com.flashcard.model.UploadResponse;
import com.flashcard.parser.PdfParser;
import com.flashcard.parser.PptParser;
import com.flashcard.parser.WordParser;
import com.flashcard.service.FirebaseService;
import com.flashcard.service.FlashcardService;
import com.flashcard.service.GeminiService;
import com.flashcard.service.JobService;
import com.flashcard.service.ParserService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Acceptance Tests validating end-to-end user journeys and quality criteria.
 */
@Tag("acceptance")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FlashcardAcceptanceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private GeminiService geminiService;

    @Mock
    private FirebaseService firebaseService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Object> redisStore = new ConcurrentHashMap<>();

    private FlashcardService flashcardService;
    private JobService jobService;

    @BeforeEach
    void setUp() {
        // In-memory simulation of Redis storage for authentic state transitions
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            redisStore.put(key, value);
            return null;
        }).when(valueOperations).set(anyString(), any());

        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return redisStore.get(key);
        }).when(valueOperations).get(anyString());

        jobService = new JobService(redisTemplate);

        ParserService parserService = new ParserService(List.of(
                new PdfParser(),
                new WordParser(),
                new PptParser()
        ));

        flashcardService = new FlashcardService(parserService, jobService, geminiService, firebaseService);
        FlashcardController controller = new FlashcardController(jobService, flashcardService, objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Acceptance Scenario 1: User uploads document, polls status, and retrieves compliant flashcards")
    void scenario1_HappyPath_UploadPollAndRetrieveCompliantFlashcards() throws Exception {
        // Step 1: Create a genuine PDF study document in memory
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(50, 700);
                cs.showText("Kubernetes is an open-source system for automating deployment, scaling, and management of containerized applications.");
                cs.endText();
            }
            doc.save(pdfStream);
        }

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "k8s-guide.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfStream.toByteArray()
        );

        String realisticLlmFlashcards = """
                [
                  {
                    "question": "What is the primary function of Kubernetes?",
                    "answer": "Automating deployment, scaling, and management of containerized applications",
                    "explanation": "Kubernetes orchestrates container lifecycles across clusters of nodes.",
                    "distractors": [
                      "Compiling Java source code into bytecode",
                      "Serving as a relational database storage engine",
                      "Routing audio signals in multimedia streaming"
                    ],
                    "incorrectExplanations": [
                      "Compiling code is handled by compilers like javac, not Kubernetes.",
                      "Relational storage is provided by databases such as PostgreSQL.",
                      "Audio routing is handled by media frameworks, not container orchestrators."
                    ],
                    "difficulty": "medium",
                    "tags": ["devops", "kubernetes", "containers"],
                    "cognitiveLevel": "understand",
                    "confidence": 0.98,
                    "sourceSnippet": "Kubernetes is an open-source system for automating deployment, scaling, and management of containerized applications."
                  }
                ]
                """;

        when(geminiService.generateFlashcards(anyString())).thenReturn(realisticLlmFlashcards);

        // Step 2: User uploads the file
        MvcResult uploadResult = mockMvc.perform(multipart("/api/flashcards/upload").file(file))
                .andExpect(status().isOk())
                .andReturn();

        UploadResponse uploadResponse = objectMapper.readValue(
                uploadResult.getResponse().getContentAsString(),
                UploadResponse.class
        );
        String jobId = uploadResponse.jobId();
        assertThat(jobId).isNotBlank();

        // Step 3: User polls status until COMPLETED
        MvcResult statusResult = mockMvc.perform(get("/api/flashcards/status/" + jobId))
                .andExpect(status().isOk())
                .andReturn();

        JobStatusResponse statusResponse = objectMapper.readValue(
                statusResult.getResponse().getContentAsString(),
                JobStatusResponse.class
        );

        assertThat(statusResponse.jobId()).isEqualTo(jobId);
        assertThat(statusResponse.status()).isEqualTo("COMPLETED");
        assertThat(statusResponse.fileName()).isEqualTo("k8s-guide.pdf");
        assertThat(statusResponse.error()).isNull();

        // Step 4: User retrieves flashcard results
        MvcResult resultMvc = mockMvc.perform(get("/api/flashcards/result/" + jobId))
                .andExpect(status().isOk())
                .andReturn();

        FlashcardResultResponse resultResponse = objectMapper.readValue(
                resultMvc.getResponse().getContentAsString(),
                FlashcardResultResponse.class
        );

        assertThat(resultResponse.jobId()).isEqualTo(jobId);
        assertThat(resultResponse.fileName()).isEqualTo("k8s-guide.pdf");
        List<Flashcard> flashcards = resultResponse.flashcards();
        assertThat(flashcards).isNotEmpty();

        // Step 5: Verify Acceptance Criteria on the Flashcards
        Flashcard card = flashcards.get(0);
        assertThat(card.getQuestion()).isNotBlank();
        assertThat(card.getAnswer()).isNotBlank();
        assertThat(card.getExplanation()).isNotBlank();

        // Must have exactly 3 distractors
        assertThat(card.getDistractors()).hasSize(3);
        assertThat(card.getDistractors()).doesNotContain(card.getAnswer());

        // Must have matching incorrect explanations
        assertThat(card.getIncorrectExplanations()).hasSize(3);

        // Cognitive level must be a valid Bloom taxonomy level
        assertThat(card.getCognitiveLevel()).isIn("remember", "understand", "apply", "analyze");

        // Confidence must be between 0.0 and 1.0
        assertThat(card.getConfidence()).isBetween(0.0, 1.0);

        // Source snippet must be present
        assertThat(card.getSourceSnippet()).isNotBlank();

        // Verify Firebase was requested to persist the deck
        verify(firebaseService, times(1)).saveDeck(eq(jobId), eq("k8s-guide.pdf"), anyString());
    }

    @Test
    @DisplayName("Acceptance Scenario 2: Unsupported file format fails cleanly with user-friendly error")
    void scenario2_UnsupportedFileFormat_FailsCleanly() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malformed-document.xyz",
                "application/octet-stream",
                new byte[]{1, 2, 3}
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/flashcards/upload").file(file))
                .andExpect(status().isOk())
                .andReturn();

        UploadResponse uploadResponse = objectMapper.readValue(
                uploadResult.getResponse().getContentAsString(),
                UploadResponse.class
        );
        String jobId = uploadResponse.jobId();

        // User polls status -> should report FAILED
        mockMvc.perform(get("/api/flashcards/status/" + jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.error").value("Unsupported file format: xyz"));

        // User attempts to retrieve results -> 409 Conflict
        mockMvc.perform(get("/api/flashcards/result/" + jobId))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Acceptance Scenario 3: Inquiring about non-existent job returns HTTP 404")
    void scenario3_NonExistentJob_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/flashcards/status/unknown-job-id"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/flashcards/result/unknown-job-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Acceptance Scenario 4: Premature result retrieval while PROCESSING returns HTTP 409 Conflict")
    void scenario4_PrematureResultRetrieval_ReturnsConflict() throws Exception {
        FlashcardJob job = FlashcardJob.builder()
                .id("processing-job")
                .fileName("draft.pdf")
                .status("PROCESSING")
                .build();

        redisStore.put("job:processing-job", job);

        mockMvc.perform(get("/api/flashcards/result/processing-job"))
                .andExpect(status().isConflict());
    }
}
