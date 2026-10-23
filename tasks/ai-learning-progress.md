# AI Learning Progress And Next Work

Created: June 13, 2026

## Current Focus

Frontend work is paused. The next sessions should focus on understanding and improving the AI flashcard-generation pipeline.

Current roadmap phase:

- **Phase 0: AI, Machine Learning, And LLM Foundations**

Implementation progress has also reached the first part of Phase 4: Gemini JSON is parsed into typed flashcards, validated, and persisted to PostgreSQL instead of Firebase.

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
  -> FlashcardService parses List<Flashcard>
  -> FlashcardValidationService validates the cards
  -> DeckPersistenceService stores the typed deck in PostgreSQL JSONB
  -> Redis job becomes COMPLETED or FAILED
```

Relevant files:

- `src/main/java/com/flashcard/service/GeminiService.java`
- `src/main/java/com/flashcard/service/FlashcardService.java`
- `src/main/java/com/flashcard/model/Flashcard.java`
- `src/main/java/com/flashcard/service/DeckPersistenceService.java`
- `src/main/java/com/flashcard/model/FlashcardDeck.java`
- `src/main/java/com/flashcard/repository/FlashcardDeckRepository.java`
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

The application now proves that Gemini returns a JSON array that maps to `Flashcard` objects and checks required text, collection sizes, and blank collection values. Remaining guardrails include:

- allowed difficulty values,
- duplicate questions and answers,
- evidence support against the source document.

Allowed cognitive levels and the inclusive `0.0` to `1.0` confidence range are now validated and covered by focused unit tests.

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
6. Store structured flashcard objects in PostgreSQL JSONB.
7. Add unit tests for valid and invalid model output.
8. Consider one controlled retry for malformed model output.

This implementation work belongs to Phase 4 of `tasks/ai-roadmap.md`. Complete the required Phase 0 concepts first, then follow the roadmap in order.

## Follow-Up AI Work

The complete production sequence is maintained in:

- `tasks/ai-roadmap.md`

After structured validation is stable:

1. Verify the persistence flow against the Neon database.
2. Refactor Redis to keep only temporary job status and progress, with a TTL.
3. Learn tokens, context windows, and token budgeting before RAG.
4. Add slide-based or token-budget chunking for long inputs.
5. Generate cards per chunk, merge results, and remove duplicates.
6. Move uploaded files to object storage instead of passing a complete `byte[]` to background work.
7. Add generation quality evaluation.
8. Add embeddings and vector search with PostgreSQL and pgvector later.
9. Build grounded RAG and an Ask My Document feature only after those foundations.
10. Add source citations and user feedback.

## Redis And Large-Document Plan

Redis is not the storage solution for a large PPT. It coordinates temporary work:

```text
upload -> object storage -> background worker -> parse by slide
       -> token-sized chunks -> Gemini batches -> PostgreSQL deck

Redis: job status and progress only
```

Near-term changes:

- `extractedText` and `flashcardsJson` have been removed from `FlashcardJob`,
- Redis now retains only job ID, status, progress, file name, and error metadata,
- job entries now expire 24 hours after their latest update,
- completed result retrieval now reads PostgreSQL rather than Redis,
- teach and implement chunking without introducing RAG,
- keep local Redis in Docker and use `REDIS_URL` for a managed production instance.

## Deferred Reliability Work

- Server-side file type and size validation
- Prompt and model configuration tests
- Gemini timeout and retry policy
- Better API error classification
- Token/input-size controls before sending extracted text
- Logging without exposing document content or secrets
