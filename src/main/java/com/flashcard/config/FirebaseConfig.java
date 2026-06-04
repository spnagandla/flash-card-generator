package com.flashcard.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Configuration class to initialize the Firebase Admin SDK using credentials 
 * fetched from Infisical at startup.
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    // This value is injected by InfisicalPropertySource during the bootstrap phase
    @Value("${FIRE_BASE_SERVICE_ACCOUNT_JSON:}")
    private String firebaseJson;

    /**
     * Creates and initializes the Firestore bean.
     * 
     * @return The Firestore instance for database operations.
     * @throws Exception If initialization fails.
     */
    @Bean
    @ConditionalOnProperty(name = "FIRE_BASE_SERVICE_ACCOUNT_JSON")
    public Firestore firestore() throws Exception {
        log.info("🚀 Initializing Firebase SDK with credentials from Infisical...");

        if (firebaseJson == null || firebaseJson.isEmpty()) {
            log.error("❌ Firebase credentials not found in environment! Check Infisical setup.");
            throw new IllegalStateException("Firebase credentials are required.");
        }

        try {
            // 1. Convert the JSON string from Infisical into a Stream
            InputStream serviceAccount = new ByteArrayInputStream(
                    firebaseJson.getBytes(StandardCharsets.UTF_8)
            );

            // 2. Build the Firebase Options
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // 3. Initialize the app (only once!)
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase App initialized successfully.");
            }

            return FirestoreClient.getFirestore();
        } catch (Exception e) {
            log.error("❌ Failed to initialize Firebase SDK", e);
            throw e;
        }
    }
}
