package com.flashcard.smoke;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashcard.controller.FlashcardController;
import com.flashcard.model.Flashcard;
import com.flashcard.model.FlashcardJob;
import com.flashcard.parser.PdfParser;
import com.flashcard.parser.PptParser;
import com.flashcard.parser.WordParser;
import com.flashcard.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("smoke")
class FlashcardSmokeTest {

    @Test
    @DisplayName("Smoke: Core application components should instantiate cleanly")
    void testApplicationComponentsInstantiate() {
        PdfParser pdfParser = new PdfParser();
        WordParser wordParser = new WordParser();
        PptParser pptParser = new PptParser();

        ParserService parserService = new ParserService(List.of(pdfParser, wordParser, pptParser));
        JobService jobService = mock(JobService.class);
        GeminiService geminiService = mock(GeminiService.class);
        FirebaseService firebaseService = mock(FirebaseService.class);

        FlashcardService flashcardService = new FlashcardService(parserService, jobService, geminiService, firebaseService);
        ObjectMapper objectMapper = new ObjectMapper();
        FlashcardController controller = new FlashcardController(jobService, flashcardService, objectMapper);

        assertThat(controller).isNotNull();
        assertThat(flashcardService).isNotNull();
        assertThat(parserService).isNotNull();
    }

    @Test
    @DisplayName("Smoke: ParserService should discover all three document parsers")
    void testParserRegistryDiscovery() {
        PdfParser pdfParser = new PdfParser();
        WordParser wordParser = new WordParser();
        PptParser pptParser = new PptParser();

        assertThat(pdfParser.supports("pdf")).isTrue();
        assertThat(wordParser.supports("docx")).isTrue();
        assertThat(pptParser.supports("pptx")).isTrue();

        ParserService parserService = new ParserService(List.of(pdfParser, wordParser, pptParser));
        assertThat(parserService).isNotNull();
    }

    @Test
    @DisplayName("Smoke: Controller endpoints should respond without internal server errors")
    void testEndpointResponsiveness() throws Exception {
        JobService jobService = mock(JobService.class);
        FlashcardService flashcardService = mock(FlashcardService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        FlashcardController controller = new FlashcardController(jobService, flashcardService, objectMapper);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/flashcards/status/smoke-check-id"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/flashcards/result/smoke-check-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Smoke: Jackson ObjectMapper should serialize and deserialize core models")
    void testJacksonSerializationSmoke() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        Flashcard card = new Flashcard(
                "What is Smoke Testing?",
                "Basic sanity test verifying critical functions work",
                "It ensures the build is stable enough for deeper testing",
                List.of("Stress test", "Unit test only", "Regression test only"),
                List.of("Not stress test", "Not unit test only", "Not regression only"),
                "easy",
                List.of("qa", "testing"),
                "understand",
                0.99,
                "Smoke testing ensures basic stability."
        );

        String json = objectMapper.writeValueAsString(card);
        assertThat(json).contains("What is Smoke Testing?");

        Flashcard deserialized = objectMapper.readValue(json, Flashcard.class);
        assertThat(deserialized.getQuestion()).isEqualTo(card.getQuestion());
        assertThat(deserialized.getAnswer()).isEqualTo(card.getAnswer());
        assertThat(deserialized.getDistractors()).hasSize(3);

        FlashcardJob job = FlashcardJob.builder()
                .id("smoke-job-1")
                .status("COMPLETED")
                .fileName("smoke.pdf")
                .flashcardsJson(json)
                .build();

        String jobJson = objectMapper.writeValueAsString(job);
        assertThat(jobJson).contains("smoke-job-1", "COMPLETED");
    }
}
