# AI Learning Progress And Next Work

Created: June 13, 2026

## Current Focus

Frontend work is paused. The next sessions should focus on understanding and improving the AI flashcard-generation pipeline.

Current roadmap phase:

- **Phase 0: AI, Machine Learning, And LLM Foundations**

Next lesson:

1. AI versus machine learning versus deep learning versus generative AI
2. Datasets, features, labels, models, training, and inference
3. Supervised versus unsupervised learning
4. Why modern LLM pretraining is primarily self-supervised
5. Conceptual introduction to embeddings and vector databases

Use the same teaching style defined in `instructions.md`:

- Explain the problem and data flow before code.
- Introduce one main AI/backend concept at a time.
- Explain every unfamiliar Java, JSON, HTTP, or AI concept.
- Let the user implement learning-focused changes unless they explicitly ask Codex to implement.
- Review the actual code after each step.

## Existing AI Pipeline

```text
Uploaded document
  -> ParserService extracts text
  -> GeminiService builds a prompt
  -> Gemini REST API generates JSON text
  -> FlashcardService stores the raw JSON string
  -> FirebaseService stores the deck
  -> Redis job becomes COMPLETED or FAILED
```

Relevant files:

- `src/main/java/com/flashcard/service/GeminiService.java`
- `src/main/java/com/flashcard/service/FlashcardService.java`
- `src/main/java/com/flashcard/model/Flashcard.java`
- `src/main/java/com/flashcard/service/FirebaseService.java`
- `src/main/java/com/flashcard/service/ParserService.java`

## AI Concepts Already Present

- Prompt engineering
- Gemini REST API request construction
- Model selection through configuration
- Asynchronous background processing with `@Async`
- Basic JSON text extraction from the Gemini API response
- Prompt-requested structured output
- Confidence, difficulty, tags, cognitive level, and source snippet fields

## Important Current Weakness

Gemini output is currently treated as a trusted raw `String`.

The prompt asks for valid JSON, but the application does not prove that:

- the response is a JSON array,
- every item maps to `Flashcard`,
- required text fields are present,
- each card has exactly three distractors,
- `incorrectExplanations` matches the distractor count,
- enum-like fields contain allowed values,
- confidence is between `0.0` and `1.0`,
- unsupported or malformed cards are rejected.

The job can therefore become `COMPLETED` with invalid AI output.

## First Implementation Lesson After Foundations

Implement structured parsing and validation before chunking, embeddings, or RAG.

Target flow:

```text
Gemini response text
  -> ObjectMapper parses List<Flashcard>
  -> application validates every card
  -> valid cards continue to persistence
  -> invalid output fails the job with a useful error
```

Recommended learning steps:

1. Understand why prompt instructions are not validation.
2. Parse Gemini JSON into `List<Flashcard>` with Jackson.
3. Validate required fields and collection sizes.
4. Validate difficulty, cognitive level, and confidence ranges.
5. Return typed flashcards from `GeminiService` instead of a raw string.
6. Store structured flashcard objects in Firestore.
7. Add unit tests for valid and invalid model output.
8. Consider one controlled retry for malformed model output.

This implementation work belongs to Phase 4 of `tasks/ai-roadmap.md`. Complete the required Phase 0 concepts first, then follow the roadmap in order.

## Follow-Up AI Work

The complete production sequence is maintained in:

- `tasks/ai-roadmap.md`

After structured validation is stable:

1. Fix Firebase failure propagation so failed persistence marks the job `FAILED`.
2. Add document chunking for long inputs.
3. Generate cards per chunk.
4. Merge results and remove duplicates.
5. Add generation quality evaluation.
6. Add embeddings and vector search.
7. Build grounded RAG and an Ask My Document feature.
8. Add source citations and user feedback.

## Deferred Reliability Work

- Server-side file type and size validation
- Prompt and model configuration tests
- Gemini timeout and retry policy
- Better API error classification
- Token/input-size controls before sending extracted text
- Logging without exposing document content or secrets
