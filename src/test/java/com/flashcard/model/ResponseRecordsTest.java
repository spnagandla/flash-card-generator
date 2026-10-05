package com.flashcard.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseRecordsTest {

    @Test
    @DisplayName("UploadResponse record should store and expose jobId")
    void testUploadResponse() {
        UploadResponse response = new UploadResponse("job-999");
        assertThat(response.jobId()).isEqualTo("job-999");
    }

    @Test
    @DisplayName("JobStatusResponse record should store and expose all status attributes")
    void testJobStatusResponse() {
        JobStatusResponse response = new JobStatusResponse("job-888", "PROCESSING", "notes.pdf", null);

        assertThat(response.jobId()).isEqualTo("job-888");
        assertThat(response.status()).isEqualTo("PROCESSING");
        assertThat(response.fileName()).isEqualTo("notes.pdf");
        assertThat(response.error()).isNull();
    }

    @Test
    @DisplayName("FlashcardResultResponse record should store and expose flashcards list")
    void testFlashcardResultResponse() {
        Flashcard card = new Flashcard();
        card.setQuestion("What is HTTP?");
        card.setAnswer("Hypertext Transfer Protocol");

        FlashcardResultResponse response = new FlashcardResultResponse("job-777", "web.pdf", List.of(card));

        assertThat(response.jobId()).isEqualTo("job-777");
        assertThat(response.fileName()).isEqualTo("web.pdf");
        assertThat(response.flashcards()).hasSize(1);
        assertThat(response.flashcards().get(0).getQuestion()).isEqualTo("What is HTTP?");
    }
}
