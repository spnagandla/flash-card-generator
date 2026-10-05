package com.flashcard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    @Value("${gemini.api.key:}")
    private String apiKey;

    public String generateFlashcards(String text) throws Exception {
        log.info("Requesting Expert Tutor flashcards from Gemini...");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is missing. Check Infisical setup.");
        }

        String systemPrompt = """
            You are an expert tutor creating study flashcards.

            Rules:
            - Return ONLY valid JSON.
            - Return a raw JSON array, not an object.
            - Do not include markdown, code fences, preamble, or extra text.
            - Create 6 to 10 high-quality multiple-choice flashcards from the user's document text.
            - Focus on important concepts, not tiny details.
            - Avoid duplicate or overly similar questions.
            - Use clear student-friendly language.
            - Keep each question under 25 words when possible.
            - Keep each explanation under 2 sentences.
            - Each card must have exactly 3 distractors.
            - Each distractor must be plausible but clearly incorrect.
            - incorrectExplanations must match the distractors order.

            Each flashcard object must use exactly these keys:
            1. "question"
            2. "answer"
            3. "explanation"
            4. "distractors"
            5. "incorrectExplanations"
            6. "difficulty" with one of these values: "easy", "medium", or "hard"
            7. "tags" as an array of 1 to 3 short topic labels
            8. "cognitiveLevel" with one of these values: "remember", "understand", "apply", or "analyze"
            9. "confidence" as a number from 0.0 to 1.0 showing how strongly the card is supported by the text
            10. "sourceSnippet" as a short quote or paraphrase from the text that supports the answer
            """;

        String userPrompt = String.format("""
            TEXT TO PROCESS:
            %s
            """, text);

        try {
            ChatClient.Builder chatClientBuilder = chatClientBuilderProvider.getIfAvailable();
            if (chatClientBuilder == null) {
                throw new IllegalStateException("Spring AI ChatClient is not configured. Check Spring AI model settings.");
            }

            String response = chatClientBuilder
                    .build()
                    .prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                throw new IllegalStateException("Gemini response did not contain generated text.");
            }

            log.info("Successfully received flashcards from Gemini.");
            return response.replace("```json", "").replace("```", "").trim();
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            throw e;
        }
    }

}
