package com.flashcard.model;

public record JobStatusResponse(
        String jobId,
        String status,
        String fileName,
        String error
) {
}
