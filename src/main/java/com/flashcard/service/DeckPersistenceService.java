package com.flashcard.service;

import com.flashcard.model.Flashcard;
import com.flashcard.model.FlashcardDeck;
import com.flashcard.repository.FlashcardDeckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeckPersistenceService {

    private final FlashcardDeckRepository flashcardDeckRepository;

    public void saveDeck(String jobId, String fileName, List<Flashcard> flashcards) {
        log.info("Saving completed flashcard deck to PostgreSQL for job: {}", jobId);
        flashcardDeckRepository.save(new FlashcardDeck(jobId, fileName, flashcards));
    }

    public Optional<FlashcardDeck> findDeck(String jobId) {
        return flashcardDeckRepository.findById(jobId);
    }
}
