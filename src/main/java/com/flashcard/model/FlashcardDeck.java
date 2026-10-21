package com.flashcard.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "flashcard_decks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlashcardDeck {

    @Id
    @Column(name = "job_id", nullable = false, updatable = false)
    private String jobId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "flashcards", nullable = false, columnDefinition = "jsonb")
    private List<Flashcard> flashcards;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public FlashcardDeck(String jobId, String fileName, List<Flashcard> flashcards) {
        this.jobId = jobId;
        this.fileName = fileName;
        this.flashcards = List.copyOf(flashcards);
        this.createdAt = Instant.now();
    }
}
