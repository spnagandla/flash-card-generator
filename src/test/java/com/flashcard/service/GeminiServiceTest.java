package com.flashcard.service;

import com.flashcard.model.Flashcard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        geminiService = new GeminiService(chatClientBuilderProvider);
    }

    @Test
    @DisplayName("generateFlashcards should throw IllegalStateException when API key is missing or blank")
    void generateFlashcards_MissingApiKey_ThrowsException() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "");

        assertThatThrownBy(() -> geminiService.generateFlashcards("sample text"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("GEMINI_API_KEY is missing. Check Infisical setup.");
    }

    @Test
    @DisplayName("generateFlashcards should throw IllegalStateException when ChatClient.Builder is not available")
    void generateFlashcards_MissingChatClientBuilder_ThrowsException() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-api-key");
        when(chatClientBuilderProvider.getIfAvailable()).thenReturn(null);

        assertThatThrownBy(() -> geminiService.generateFlashcards("sample text"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Spring AI ChatClient is not configured. Check Spring AI model settings.");
    }

    @Test
    @DisplayName("generateFlashcards should throw IllegalStateException when LLM returns no flashcards")
    void generateFlashcards_EmptyResponse_ThrowsException() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-api-key");
        when(chatClientBuilderProvider.getIfAvailable()).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(any(ParameterizedTypeReference.class))).thenReturn(List.of());

        assertThatThrownBy(() -> geminiService.generateFlashcards("sample text"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Gemini response did not contain generated flashcards.");
    }

    @Test
    @DisplayName("generateFlashcards should return typed flashcards")
    void generateFlashcards_SuccessfulResponse_ReturnsTypedFlashcards() throws Exception {
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-api-key");
        when(chatClientBuilderProvider.getIfAvailable()).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        Flashcard flashcard = new Flashcard();
        flashcard.setQuestion("What is AI?");
        flashcard.setAnswer("Artificial Intelligence");
        when(callResponseSpec.entity(any(ParameterizedTypeReference.class)))
                .thenReturn(List.of(flashcard));

        List<Flashcard> result = geminiService.generateFlashcards("AI is Artificial Intelligence");

        assertThat(result).containsExactly(flashcard);
    }

    @Test
    @DisplayName("generateFlashcards should rethrow exception when ChatClient invocation fails")
    void generateFlashcards_ChatClientThrows_RethrowsException() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-api-key");
        when(chatClientBuilderProvider.getIfAvailable()).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("API Connection timeout"));

        assertThatThrownBy(() -> geminiService.generateFlashcards("text"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("API Connection timeout");
    }
}
