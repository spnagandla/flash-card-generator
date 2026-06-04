package com.flashcard.service;

import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService {

   private final ObjectProvider<Firestore> firestoreProvider;

   public void saveDeck(String jobId, String fileName, String flashcardsJson) {
        log.info("Saving completed flashcard deck to Firebase for job: {}", jobId);

        Firestore firestore = firestoreProvider.getIfAvailable();
        if (firestore == null) {
            log.warn("Firestore is not configured. Skipping Firebase save for job: {}", jobId);
            return;
        }

        // 1. Prepare the data to save
        Map<String, Object> deckData = Map.of(
              "jobId", jobId,
              "fileName", fileName,
              "flashcards", flashcardsJson,
              "createdAt", System.currentTimeMillis()
              );
        // 2. Save to the "decks" collection
        // .set() is asynchronous, but we can call .get() if we want to wait for it.
        try {
                firestore.collection("decks").document(jobId).set(deckData).get();
                log.info("✅ Deck successfully saved to Firestore.");
            } catch (Exception e) {
                log.error("❌ Failed to save deck to Firebase", e);
        }
   }
   }
