package com.flashcard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

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
    @DisplayName("generateFlashcards should throw IllegalStateException when LLM response is empty or blank")
    void generateFlashcards_EmptyResponse_ThrowsException() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-api-key");
        when(chatClientBuilderProvider.getIfAvailable()).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("   ");

        assertThatThrownBy(() -> geminiService.generateFlashcards("sample text"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Gemini response did not contain generated text.");
    }

    @Test
    @DisplayName("generateFlashcards should strip markdown code fences and return clean JSON")
    void generateFlashcards_SuccessfulResponse_StripsMarkdown() throws Exception {
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-api-key");
        when(chatClientBuilderProvider.getIfAvailable()).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        String rawResponse = """
                ```json
                [
                  {"question": "What is AI?", "answer": "Artificial Intelligence"}
                ]
                ```
                """;
        when(callResponseSpec.content()).thenReturn(rawResponse);

        String result = geminiService.generateFlashcards("AI is Artificial Intelligence");

        assertThat(result).isEqualTo("""
                [
                  {"question": "What is AI?", "answer": "Artificial Intelligence"}
                ]""");
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
        when(requestSpec.call()).thenThrow(new RuntimeException("API Connection timeout"));

        assertThatThrownBy(() -> geminiService.generateFlashcards("text"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("API Connection timeout");
    }
}
