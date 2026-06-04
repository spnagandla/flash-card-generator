package com.flashcard.model;

import java.util.List;

public record FlashcardResultResponse(
        String jobId,
        String fileName,
        List<Flashcard> flashcards
) {
}
