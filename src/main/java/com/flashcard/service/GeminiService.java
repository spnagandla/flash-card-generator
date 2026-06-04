package com.flashcard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    public String generateFlashcards(String text) throws Exception {
        log.info("Requesting Expert Tutor flashcards from Gemini...");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is missing. Check Infisical setup.");
        }

        String prompt = String.format("""
            You are an expert tutor creating study flashcards.

            Create 6 to 10 high-quality multiple-choice flashcards from the text.

            Rules:
            - Return ONLY valid JSON.
            - Return a raw JSON array, not an object.
            - Do not include markdown, code fences, preamble, or extra text.
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

            TEXT TO PROCESS:
            %s
            """, text);

        Map<String, Object> request = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        try {
            String response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);

            String result = extractText(response);
            log.info("Successfully received flashcards from Gemini.");
            return result.replace("```json", "").replace("```", "").trim();
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            throw e;
        }
    }

    private String extractText(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        JsonNode textNode = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new IllegalStateException("Gemini response did not contain generated text.");
        }

        return textNode.asText();
    }
}
