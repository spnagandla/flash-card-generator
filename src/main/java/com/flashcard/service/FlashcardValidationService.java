package com.flashcard.service;

import com.flashcard.model.Flashcard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Slf4j
public class FlashcardValidationService {

    private static final int MAX_FLASHCARDS = 30;

    private static final Set<String> ALLOWED_DIFFICULTIES = Set.of(
            "easy",
            "medium",
            "hard"
    );

    private static final Set<String> ALLOWED_COGNITIVE_LEVELS = Set.of(
            "remember",
            "understand",
            "apply",
            "analyze"
    );

    private static final Set<String> ALLOWED_GROUNDING_TYPES = Set.of(
            "document",
            "enrichment"
    );

    public List<Flashcard> validate(List<Flashcard> flashcards) {
        if (flashcards == null || flashcards.isEmpty()) {
            throw new IllegalArgumentException("Gemini returned no flashcards.");
        }

        if (flashcards.size() > MAX_FLASHCARDS) {
            throw new IllegalArgumentException(
                    "Gemini returned too many flashcards (maximum " + MAX_FLASHCARDS + ")."
            );
        }

        List<Flashcard> validFlashcards = new ArrayList<>();
        IllegalArgumentException firstValidationFailure = null;

        for (int index = 0; index < flashcards.size(); index++) {
            Flashcard flashcard = flashcards.get(index);

            try {
                validateCard(flashcard);
                validFlashcards.add(flashcard);
            } catch (IllegalArgumentException exception) {
                if (firstValidationFailure == null) {
                    firstValidationFailure = exception;
                }
                log.warn("Dropping invalid flashcard at index {}: {}", index, exception.getMessage());
            }
        }

        if (validFlashcards.isEmpty()) {
            throw firstValidationFailure != null
                    ? firstValidationFailure
                    : new IllegalArgumentException("Gemini returned no valid flashcards.");
        }

        long documentCount = validFlashcards.stream()
                .filter(card -> card.getGroundingType().equals("document"))
                .count();
        int allowedEnrichmentCount = (int) documentCount / 4;
        int keptEnrichmentCount = 0;
        List<Flashcard> cleanedFlashcards = new ArrayList<>();

        for (Flashcard flashcard : validFlashcards) {
            if (flashcard.getGroundingType().equals("enrichment")) {
                if (keptEnrichmentCount >= allowedEnrichmentCount) {
                    log.warn("Dropping enrichment flashcard beyond the 20% deck limit: {}", flashcard.getQuestion());
                    continue;
                }
                keptEnrichmentCount++;
            }
            cleanedFlashcards.add(flashcard);
        }

        if (cleanedFlashcards.isEmpty()) {
            throw new IllegalArgumentException("Gemini returned no usable document-grounded flashcards.");
        }

        return List.copyOf(cleanedFlashcards);
    }

    private void validateCard(Flashcard flashcard) {
        if (flashcard == null) {
            throw new IllegalArgumentException("Flashcard must not be null.");
        }

        if (isBlank(flashcard.getQuestion())) {
            throw new IllegalArgumentException("Flashcard question is required.");
        }

        if (isBlank(flashcard.getAnswer())) {
            throw new IllegalArgumentException("Flashcard answer is required.");
        }

        if (isBlank(flashcard.getExplanation())) {
            throw new IllegalArgumentException("Flashcard explanation is required.");
        }

        if (isBlank(flashcard.getDifficulty())) {
            throw new IllegalArgumentException("Flashcard difficulty is required.");
        }

        String difficulty = flashcard.getDifficulty().strip().toLowerCase(Locale.ROOT);
        if (!ALLOWED_DIFFICULTIES.contains(difficulty)) {
            throw new IllegalArgumentException(
                    "Flashcard difficulty must be one of: easy, medium, hard."
            );
        }
        flashcard.setDifficulty(difficulty);

        if (isBlank(flashcard.getCognitiveLevel())) {
            throw new IllegalArgumentException("Flashcard cognitive level is required.");
        }

        if (!ALLOWED_COGNITIVE_LEVELS.contains(flashcard.getCognitiveLevel())) {
            throw new IllegalArgumentException(
                    "Flashcard cognitive level must be one of: remember, understand, apply, analyze."
            );
        }

        if (isBlank(flashcard.getGroundingType())) {
            throw new IllegalArgumentException("Flashcard grounding type is required.");
        }

        String groundingType = flashcard.getGroundingType().strip().toLowerCase(Locale.ROOT);
        if (!ALLOWED_GROUNDING_TYPES.contains(groundingType)) {
            throw new IllegalArgumentException(
                    "Flashcard grounding type must be one of: document, enrichment."
            );
        }
        flashcard.setGroundingType(groundingType);

        Double confidence = flashcard.getConfidence();
        if (confidence == null || !Double.isFinite(confidence) || confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("Flashcard confidence must be between 0.0 and 1.0.");
        }

        if (isBlank(flashcard.getSourceSnippet())) {
            throw new IllegalArgumentException("Flashcard source snippet is required.");
        }

        if (flashcard.getDistractors() == null || flashcard.getDistractors().size() != 3) {
            throw new IllegalArgumentException("Flashcard must have exactly 3 distractors.");
        }

        if (flashcard.getIncorrectExplanations() == null || flashcard.getIncorrectExplanations().size() != 3) {
            throw new IllegalArgumentException("Flashcard must have exactly 3 incorrect explanations.");
        }

        if (flashcard.getTags() == null || flashcard.getTags().isEmpty() || flashcard.getTags().size() > 3) {
            throw new IllegalArgumentException("Flashcard must have 1 to 3 tags.");
        }

        if (containsBlank(flashcard.getDistractors())) {
            throw new IllegalArgumentException("Flashcard distractors must not be blank.");
        }

        if (containsBlank(flashcard.getIncorrectExplanations())) {
            throw new IllegalArgumentException("Flashcard incorrect explanations must not be blank.");
        }

        if (containsBlank(flashcard.getTags())) {
            throw new IllegalArgumentException("Flashcard tags must not be blank.");
        }

    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean containsBlank(List<String> values) {
        return values.stream().anyMatch(this::isBlank);

    }
}
