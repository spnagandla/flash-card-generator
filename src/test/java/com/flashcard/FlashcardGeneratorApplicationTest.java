package com.flashcard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.assertj.core.api.Assertions.assertThat;

class FlashcardGeneratorApplicationTest {

    @Test
    @DisplayName("FlashcardGeneratorApplication should have required Spring Boot annotations")
    void testApplicationAnnotations() {
        Class<FlashcardGeneratorApplication> appClass = FlashcardGeneratorApplication.class;

        assertThat(appClass.isAnnotationPresent(SpringBootApplication.class)).isTrue();
        assertThat(appClass.isAnnotationPresent(EnableAsync.class)).isTrue();
    }

    @Test
    @DisplayName("FlashcardGeneratorApplication instance should be constructable")
    void testApplicationInstantiation() {
        FlashcardGeneratorApplication app = new FlashcardGeneratorApplication();
        assertThat(app).isNotNull();
    }
}
