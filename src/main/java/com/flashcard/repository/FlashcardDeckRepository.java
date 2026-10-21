package com.flashcard.repository;

import com.flashcard.model.FlashcardDeck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardDeckRepository extends JpaRepository<FlashcardDeck, String> {
}
