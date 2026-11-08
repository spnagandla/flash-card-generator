package com.flashcard.service;

import com.flashcard.model.Flashcard;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
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

    public void validate(List<Flashcard> flashcards) {
        if(flashcards == null || flashcards.isEmpty()) {
            throw new IllegalArgumentException("Gemini returned no flashcards.");
        }

        if (flashcards.size() > MAX_FLASHCARDS) {
            throw new IllegalArgumentException(
                    "Gemini returned too many flashcards (maximum " + MAX_FLASHCARDS + ")."
            );
        }

        int enrichmentCount = 0;

        for(Flashcard flashcard : flashcards) {

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
            if (groundingType.equals("enrichment")) {
                enrichmentCount++;
            }

            Double confidence = flashcard.getConfidence();
            if (confidence == null || !Double.isFinite(confidence) || confidence < 0.70 || confidence > 0.99) {
                throw new IllegalArgumentException("Flashcard confidence must be between 0.70 and 0.99.");
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

        if (enrichmentCount * 5 > flashcards.size()) {
            throw new IllegalArgumentException(
                    "Enrichment flashcards must not exceed 20% of the deck."
            );
        }
    }

    private boolean isBlank(String value){
            return value == null || value.isBlank();
    }

    private boolean containsBlank(List<String> values) {
        return values.stream().anyMatch(String::isBlank);

    }
}
