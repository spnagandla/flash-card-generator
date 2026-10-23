package com.flashcard.service;

import com.flashcard.model.Flashcard;
import com.flashcard.model.FlashcardDeck;
import com.flashcard.repository.FlashcardDeckRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeckPersistenceServiceTest {

    @Mock
    private FlashcardDeckRepository flashcardDeckRepository;

    @InjectMocks
    private DeckPersistenceService deckPersistenceService;

    @Test
    @DisplayName("saveDeck should persist a typed PostgreSQL deck")
    void saveDeck_ValidDeck_PersistsEntity() {
        Flashcard card = new Flashcard(
                "Question?",
                "Answer",
                "Explanation",
                List.of("Wrong 1", "Wrong 2", "Wrong 3"),
                List.of("Why 1", "Why 2", "Why 3"),
                "easy",
                List.of("topic"),
                "remember",
                0.95,
                "Source"
        );

        deckPersistenceService.saveDeck("job-1", "notes.pdf", List.of(card));

        ArgumentCaptor<FlashcardDeck> captor = ArgumentCaptor.forClass(FlashcardDeck.class);
        verify(flashcardDeckRepository).save(captor.capture());

        FlashcardDeck savedDeck = captor.getValue();
        assertThat(savedDeck.getJobId()).isEqualTo("job-1");
        assertThat(savedDeck.getFileName()).isEqualTo("notes.pdf");
        assertThat(savedDeck.getFlashcards()).containsExactly(card);
        assertThat(savedDeck.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findDeck should retrieve a completed deck by job ID")
    void findDeck_ExistingJob_ReturnsDeck() {
        FlashcardDeck deck = new FlashcardDeck("job-2", "slides.pptx", List.of(new Flashcard()));
        when(flashcardDeckRepository.findById("job-2")).thenReturn(Optional.of(deck));

        Optional<FlashcardDeck> result = deckPersistenceService.findDeck("job-2");

        assertThat(result).contains(deck);
        verify(flashcardDeckRepository).findById("job-2");
    }
}
