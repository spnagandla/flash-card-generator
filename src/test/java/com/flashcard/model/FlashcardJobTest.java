package com.flashcard.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class FlashcardJobTest {

    @Test
    @DisplayName("FlashcardJob builder and getters/setters should work correctly")
    void testFlashcardJobBuilderAndAccessors() {
        FlashcardJob job = FlashcardJob.builder()
                .id("job-abc")
                .status("PROCESSING")
                .fileName("deck.pdf")
                .flashcardsJson("[{\"question\":\"Q1\"}]")
                .extractedText("Sample document text")
                .error(null)
                .build();

        assertThat(job.getId()).isEqualTo("job-abc");
        assertThat(job.getStatus()).isEqualTo("PROCESSING");
        assertThat(job.getFileName()).isEqualTo("deck.pdf");
        assertThat(job.getFlashcardsJson()).isEqualTo("[{\"question\":\"Q1\"}]");
        assertThat(job.getExtractedText()).isEqualTo("Sample document text");
        assertThat(job.getError()).isNull();

        job.setStatus("FAILED");
        job.setError("Something went wrong");
        assertThat(job.getStatus()).isEqualTo("FAILED");
        assertThat(job.getError()).isEqualTo("Something went wrong");
    }

    @Test
    @DisplayName("FlashcardJob should be serializable")
    void testFlashcardJobSerialization() throws Exception {
        FlashcardJob job = FlashcardJob.builder()
                .id("job-serial")
                .status("COMPLETED")
                .fileName("lecture.docx")
                .flashcardsJson("[]")
                .extractedText("Text")
                .build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(job);
        }

        FlashcardJob deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            deserialized = (FlashcardJob) ois.readObject();
        }

        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getId()).isEqualTo("job-serial");
        assertThat(deserialized.getStatus()).isEqualTo("COMPLETED");
        assertThat(deserialized.getFileName()).isEqualTo("lecture.docx");
        assertThat(deserialized).isEqualTo(job);
    }
}
