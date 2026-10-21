package com.flashcard.service;

import com.flashcard.model.Flashcard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlashcardValidationServiceTest {

    private final FlashcardValidationService validationService = new FlashcardValidationService();

    @ParameterizedTest
    @ValueSource(strings = {"remember", "understand", "apply", "analyze"})
    void acceptsSupportedCognitiveLevels(String cognitiveLevel) {
        Flashcard flashcard = validFlashcard();
        flashcard.setCognitiveLevel(cognitiveLevel);

        assertThatCode(() -> validationService.validate(List.of(flashcard)))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsUnsupportedCognitiveLevel() {
        Flashcard flashcard = validFlashcard();
        flashcard.setCognitiveLevel("create");

        assertThatThrownBy(() -> validationService.validate(List.of(flashcard)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cognitive level must be one of");
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1.0})
    void acceptsConfidenceBoundaryValues(double confidence) {
        Flashcard flashcard = validFlashcard();
        flashcard.setConfidence(confidence);

        assertThatCode(() -> validationService.validate(List.of(flashcard)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, 1.01, Double.NaN, Double.POSITIVE_INFINITY})
    void rejectsConfidenceOutsideAllowedRange(double confidence) {
        Flashcard flashcard = validFlashcard();
        flashcard.setConfidence(confidence);

        assertThatThrownBy(() -> validationService.validate(List.of(flashcard)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("confidence must be between 0.0 and 1.0");
    }

    @Test
    void rejectsMissingConfidence() {
        Flashcard flashcard = validFlashcard();
        flashcard.setConfidence(null);

        assertThatThrownBy(() -> validationService.validate(List.of(flashcard)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("confidence must be between 0.0 and 1.0");
    }

    private Flashcard validFlashcard() {
        return new Flashcard(
                "What is polymorphism?",
                "The ability to use one interface for multiple implementations.",
                "Different object types can respond through the same contract.",
                List.of("Encapsulation", "Compilation", "Serialization"),
                List.of("A different OOP concept", "A build step", "A data-formatting process"),
                "medium",
                List.of("java", "oop"),
                "understand",
                0.9,
                "Polymorphism allows objects of different types to share a common interface."
        );
    }
}
