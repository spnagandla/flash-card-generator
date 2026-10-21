package com.flashcard.service;

import com.flashcard.model.Flashcard;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class FlashcardValidationService {

    private static final Set<String> ALLOWED_COGNITIVE_LEVELS = Set.of(
            "remember",
            "understand",
            "apply",
            "analyze"
    );

    public void validate(List<Flashcard> flashcards) {
        if(flashcards == null || flashcards.isEmpty()) {
            throw new IllegalArgumentException("Gemini returned no flashcards.");
        }

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

            if (isBlank(flashcard.getCognitiveLevel())) {
                throw new IllegalArgumentException("Flashcard cognitive level is required.");
            }

            if (!ALLOWED_COGNITIVE_LEVELS.contains(flashcard.getCognitiveLevel())) {
                throw new IllegalArgumentException(
                        "Flashcard cognitive level must be one of: remember, understand, apply, analyze."
                );
            }

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
    }

    private boolean isBlank(String value){
            return value == null || value.isBlank();
    }

    private boolean containsBlank(List<String> values) {
        return values.stream().anyMatch(String::isBlank);

    }
}
