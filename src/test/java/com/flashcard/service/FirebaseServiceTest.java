package com.flashcard.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseServiceTest {

    @Mock
    private ObjectProvider<Firestore> firestoreProvider;

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private ApiFuture<WriteResult> apiFuture;

    private FirebaseService firebaseService;

    @BeforeEach
    void setUp() {
        firebaseService = new FirebaseService(firestoreProvider);
    }

    @Test
    @DisplayName("saveDeck should gracefully skip when Firestore bean is not available")
    void saveDeck_FirestoreNotConfigured_SkipsGracefully() {
        when(firestoreProvider.getIfAvailable()).thenReturn(null);

        assertThatCode(() -> firebaseService.saveDeck("job-1", "doc.pdf", "[]"))
                .doesNotThrowAnyException();

        verifyNoInteractions(firestore);
    }

    @Test
    @DisplayName("saveDeck should construct deckData and call Firestore set().get()")
    void saveDeck_FirestoreAvailable_SavesSuccessfully() throws Exception {
        when(firestoreProvider.getIfAvailable()).thenReturn(firestore);
        when(firestore.collection("decks")).thenReturn(collectionReference);
        when(collectionReference.document("job-1")).thenReturn(documentReference);
        when(documentReference.set(any(Map.class))).thenReturn(apiFuture);
        when(apiFuture.get()).thenReturn(mock(WriteResult.class));

        firebaseService.saveDeck("job-1", "sample.pdf", "[{\"question\":\"test\"}]");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(documentReference).set(mapCaptor.capture());

        Map<String, Object> savedData = mapCaptor.getValue();
        assertThat(savedData.get("jobId")).isEqualTo("job-1");
        assertThat(savedData.get("fileName")).isEqualTo("sample.pdf");
        assertThat(savedData.get("flashcards")).isEqualTo("[{\"question\":\"test\"}]");
        assertThat(savedData.get("createdAt")).isNotNull();

        verify(apiFuture, times(1)).get();
    }

    @Test
    @DisplayName("saveDeck should catch and handle exception when Firestore write fails")
    void saveDeck_FirestoreThrowsException_HandledGracefully() throws Exception {
        when(firestoreProvider.getIfAvailable()).thenReturn(firestore);
        when(firestore.collection("decks")).thenReturn(collectionReference);
        when(collectionReference.document("job-err")).thenReturn(documentReference);
        when(documentReference.set(any(Map.class))).thenReturn(apiFuture);
        when(apiFuture.get()).thenThrow(new ExecutionException("Permission denied", new RuntimeException()));

        // Should log error and not throw to caller
        assertThatCode(() -> firebaseService.saveDeck("job-err", "doc.pdf", "[]"))
                .doesNotThrowAnyException();
    }
}
