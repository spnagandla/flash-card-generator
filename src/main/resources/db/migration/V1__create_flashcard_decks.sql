CREATE TABLE flashcard_decks (
    job_id VARCHAR(255) PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    flashcards JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
