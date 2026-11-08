package com.flashcard.service;

import com.flashcard.model.Flashcard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlashcardValidationServiceTest {

    private final FlashcardValidationService validationService = new FlashcardValidationService();

    @Test
    void acceptsUpToMaximumFlashcards() {
        List<Flashcard> cards = java.util.Collections.nCopies(30, validFlashcard());

        assertThatCode(() -> validationService.validate(cards))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsMoreThanMaximumFlashcards() {
        List<Flashcard> cards = java.util.Collections.nCopies(31, validFlashcard());

        assertThatThrownBy(() -> validationService.validate(cards))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("too many flashcards");
    }

    @ParameterizedTest
    @ValueSource(strings = {"easy", "medium", "hard"})
    void acceptsSupportedDifficulties(String difficulty) {
        Flashcard flashcard = validFlashcard();
        flashcard.setDifficulty(difficulty);

        assertThatCode(() -> validationService.validate(List.of(flashcard)))
                .doesNotThrowAnyException();
    }

    @Test
    void normalizesDifficultyCaseAndWhitespace() {
        Flashcard flashcard = validFlashcard();
        flashcard.setDifficulty("  Medium ");

        validationService.validate(List.of(flashcard));

        assertThat(flashcard.getDifficulty()).isEqualTo("medium");
    }

    @ParameterizedTest
    @ValueSource(strings = {"moderate", "very hard", "1"})
    void rejectsUnsupportedDifficulty(String difficulty) {
        Flashcard flashcard = validFlashcard();
        flashcard.setDifficulty(difficulty);

        assertThatThrownBy(() -> validationService.validate(List.of(flashcard)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("difficulty must be one of");
    }

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
    @ValueSource(strings = {"document", "enrichment"})
    void acceptsSupportedGroundingTypes(String groundingType) {
        Flashcard flashcard = validFlashcard();
        flashcard.setGroundingType(groundingType);

        List<Flashcard> cards = groundingType.equals("enrichment")
                ? List.of(flashcard, validFlashcard(), validFlashcard(), validFlashcard(), validFlashcard())
                : List.of(flashcard);

        assertThatCode(() -> validationService.validate(cards))
                .doesNotThrowAnyException();
    }

    @Test
    void normalizesGroundingTypeCaseAndWhitespace() {
        Flashcard flashcard = validFlashcard();
        flashcard.setGroundingType("  Document ");

        validationService.validate(List.of(flashcard));

        assertThat(flashcard.getGroundingType()).isEqualTo("document");
    }

    @Test
    void rejectsMissingGroundingType() {
        Flashcard flashcard = validFlashcard();
        flashcard.setGroundingType(null);

        assertThatThrownBy(() -> validationService.validate(List.of(flashcard)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("grounding type is required");
    }

    @Test
    void rejectsUnsupportedGroundingType() {
        Flashcard flashcard = validFlashcard();
        flashcard.setGroundingType("external");

        assertThatThrownBy(() -> validationService.validate(List.of(flashcard)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("grounding type must be one of");
    }

    @Test
    void acceptsAtMostTwentyPercentEnrichmentCards() {
        Flashcard enrichment = validFlashcard();
        enrichment.setGroundingType("enrichment");

        List<Flashcard> cards = List.of(
                enrichment,
                validFlashcard(),
                validFlashcard(),
                validFlashcard(),
                validFlashcard()
        );

        assertThatCode(() -> validationService.validate(cards))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsMoreThanTwentyPercentEnrichmentCards() {
        Flashcard enrichment = validFlashcard();
        enrichment.setGroundingType("enrichment");

        List<Flashcard> cards = List.of(
                enrichment,
                validFlashcard(),
                validFlashcard(),
                validFlashcard()
        );

        assertThatThrownBy(() -> validationService.validate(cards))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not exceed 20%");
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.70, 0.99})
    void acceptsConfidenceValuesWithinSupportedRange(double confidence) {
        Flashcard flashcard = validFlashcard();
        flashcard.setConfidence(confidence);

        assertThatCode(() -> validationService.validate(List.of(flashcard)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.69, 1.0, 1.01, Double.NaN, Double.POSITIVE_INFINITY})
    void rejectsConfidenceOutsideAllowedRange(double confidence) {
        Flashcard flashcard = validFlashcard();
        flashcard.setConfidence(confidence);

        assertThatThrownBy(() -> validationService.validate(List.of(flashcard)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("confidence must be between 0.70 and 0.99");
    }

    @Test
    void rejectsMissingConfidence() {
        Flashcard flashcard = validFlashcard();
        flashcard.setConfidence(null);

        assertThatThrownBy(() -> validationService.validate(List.of(flashcard)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("confidence must be between 0.70 and 0.99");
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
                "document",
                0.9,
                "Polymorphism allows objects of different types to share a common interface."
        );
    }
}
