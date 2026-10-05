package com.flashcard.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FirebaseConfigTest {

    @Test
    @DisplayName("firestore bean method should throw IllegalStateException when credentials string is null")
    void firestore_NullCredentials_ThrowsException() {
        FirebaseConfig config = new FirebaseConfig();
        ReflectionTestUtils.setField(config, "firebaseJson", null);

        assertThatThrownBy(config::firestore)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Firebase credentials are required.");
    }

    @Test
    @DisplayName("firestore bean method should throw IllegalStateException when credentials string is empty")
    void firestore_EmptyCredentials_ThrowsException() {
        FirebaseConfig config = new FirebaseConfig();
        ReflectionTestUtils.setField(config, "firebaseJson", "");

        assertThatThrownBy(config::firestore)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Firebase credentials are required.");
    }

    @Test
    @DisplayName("firestore bean method should throw Exception when credentials JSON is invalid")
    void firestore_InvalidCredentials_ThrowsException() {
        FirebaseConfig config = new FirebaseConfig();
        ReflectionTestUtils.setField(config, "firebaseJson", "invalid-json-content");

        assertThatThrownBy(config::firestore)
                .isInstanceOf(Exception.class);
    }
}
