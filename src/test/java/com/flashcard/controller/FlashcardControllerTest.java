package com.flashcard.controller;

import com.flashcard.model.Flashcard;
import com.flashcard.model.FlashcardDeck;
import com.flashcard.model.FlashcardJob;
import com.flashcard.service.DeckPersistenceService;
import com.flashcard.service.FlashcardService;
import com.flashcard.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FlashcardControllerTest {

    @Mock
    private JobService jobService;

    @Mock
    private FlashcardService flashcardService;

    @Mock
    private DeckPersistenceService deckPersistenceService;

    @InjectMocks
    private FlashcardController flashcardController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(flashcardController).build();
    }

    @Test
    @DisplayName("POST /api/flashcards/upload should start processing and return jobId")
    void upload_Success() throws Exception {
        byte[] content = "test content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                content
        );

        when(jobService.createJob("sample.pdf")).thenReturn("job-123");

        mockMvc.perform(multipart("/api/flashcards/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value("job-123"));

        verify(jobService, times(1)).createJob("sample.pdf");
        verify(flashcardService, times(1)).processFlashcards(eq("job-123"), eq(content));
    }

    @Test
    @DisplayName("GET /api/flashcards/status/{jobId} should return job status when job exists")
    void getStatus_JobFound() throws Exception {
        FlashcardJob job = FlashcardJob.builder()
                .id("job-123")
                .status("PROCESSING")
                .fileName("sample.pdf")
                .progress(10)
                .error(null)
                .build();

        when(jobService.getJob("job-123")).thenReturn(job);

        mockMvc.perform(get("/api/flashcards/status/job-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value("job-123"))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.fileName").value("sample.pdf"))
                .andExpect(jsonPath("$.progress").value(10))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(jobService, times(1)).getJob("job-123");
    }

    @Test
    @DisplayName("GET /api/flashcards/status/{jobId} should return 404 when job does not exist")
    void getStatus_JobNotFound() throws Exception {
        when(jobService.getJob("non-existent")).thenReturn(null);

        mockMvc.perform(get("/api/flashcards/status/non-existent"))
                .andExpect(status().isNotFound());

        verify(jobService, times(1)).getJob("non-existent");
    }

    @Test
    @DisplayName("GET /api/flashcards/result/{jobId} should return 404 when job does not exist")
    void getResult_JobNotFound() throws Exception {
        when(deckPersistenceService.findDeck("non-existent")).thenReturn(Optional.empty());
        when(jobService.getJob("non-existent")).thenReturn(null);

        mockMvc.perform(get("/api/flashcards/result/non-existent"))
                .andExpect(status().isNotFound());

        verify(jobService, times(1)).getJob("non-existent");
    }

    @Test
    @DisplayName("GET /api/flashcards/result/{jobId} should return 409 Conflict when job is not COMPLETED")
    void getResult_JobNotCompleted() throws Exception {
        FlashcardJob job = FlashcardJob.builder()
                .id("job-123")
                .status("PROCESSING")
                .fileName("sample.pdf")
                .build();

        when(deckPersistenceService.findDeck("job-123")).thenReturn(Optional.empty());
        when(jobService.getJob("job-123")).thenReturn(job);

        mockMvc.perform(get("/api/flashcards/result/job-123"))
                .andExpect(status().isConflict());

        verify(jobService, times(1)).getJob("job-123");
    }

    @Test
    @DisplayName("GET /api/flashcards/result/{jobId} should return 200 and flashcards when job is COMPLETED")
    void getResult_Success() throws Exception {
        Flashcard flashcard = new Flashcard(
                "What is Spring Boot?",
                "A Java framework",
                "It simplifies Spring development",
                List.of("Python framework", "C++ library", "Database"),
                List.of("Wrong", "Wrong", "Wrong"),
                "easy",
                List.of("spring"),
                "remember",
                0.95,
                "Spring Boot makes it easy..."
        );
        FlashcardDeck deck = new FlashcardDeck("job-123", "sample.pdf", List.of(flashcard));

        when(deckPersistenceService.findDeck("job-123")).thenReturn(Optional.of(deck));

        mockMvc.perform(get("/api/flashcards/result/job-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value("job-123"))
                .andExpect(jsonPath("$.fileName").value("sample.pdf"))
                .andExpect(jsonPath("$.flashcards").isArray())
                .andExpect(jsonPath("$.flashcards[0].question").value("What is Spring Boot?"))
                .andExpect(jsonPath("$.flashcards[0].answer").value("A Java framework"))
                .andExpect(jsonPath("$.flashcards[0].confidence").value(0.95));

        verify(deckPersistenceService, times(1)).findDeck("job-123");
        verifyNoInteractions(jobService);
    }
}
