# Session Log

This file records where work stopped so future sessions can resume quickly.

## 2026-10-14 12:55 AM CDT

### Where We Stopped

- The user asked to resume from where the previous session left off.
- The assistant did not have access to the previous chat transcript.
- Attempts to inspect the repository with shell commands failed due to a tool startup error: `helper_unknown_error: setup refresh had errors`.
- No active saved goal was present.

### Next Step

- User should provide a short reminder of the last task or issue being worked on.
- Once shell access is working again, inspect repository state with:
  - `git status --short`
  - `git log --oneline -5`
  - `rg --files`

## 2026-10-14 12:56 AM CDT

### Current Worktree Read

- `FlashcardValidationService.java` is the main current code change.
- The file was staged as a new empty class, then expanded in the working tree into a Spring `@Service` with validation rules for generated flashcards.
- The validator checks that Gemini returns at least one card, required fields are present, there are exactly 3 distractors and 3 incorrect explanations, and tags contain 1 to 3 nonblank values.
- `FlashcardService` has not yet been wired to use this validator; it still saves Gemini's raw JSON directly to the job and Firebase.

### Likely Goal

- Harden the flashcard generation flow so malformed Gemini responses fail fast instead of being saved as completed decks.

## 2026-10-17

### PostgreSQL Migration

- Replaced Firebase/Firestore with Spring Data JPA and PostgreSQL.
- Added Flyway migration `V1__create_flashcard_decks.sql`.
- Added typed JSONB persistence through `FlashcardDeck`, `FlashcardDeckRepository`, and `DeckPersistenceService`.
- Wired Gemini JSON parsing and `FlashcardValidationService` into `FlashcardService` before persistence.
- Added local PostgreSQL to Docker Compose and Neon-compatible environment configuration.
- Removed Firebase production code, dependency, and tests.
- IDE build and changed unit, smoke, and acceptance tests pass.

### Next Step

- Configure `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` using the Neon connection details and run an integration test against the real database.
- Then complete the remaining guardrails for difficulty, cognitive level, confidence, and duplicates.

## 2026-10-20

### Neon Connection Debugging

- Infisical now supplies `NEON_DATABASE_URL`, `NEON_DATABASE_USERNAME`, and `NEON_DATABASE_PASSWORD` to Spring's corresponding `spring.datasource.*` properties.
- The original Neon URL included `username:password@host`, which is not accepted in this JDBC URL configuration. It was corrected to contain only the JDBC scheme, Neon host, database, and SSL parameters.
- The latest application run connected successfully to Neon PostgreSQL through HikariCP. The URL, username, and password problem is resolved.
- The temporary password diagnostic logging was removed. Raw passwords should never be printed.
- Because credentials appeared in earlier console/debug output, rotate the Neon database password and the Infisical client secret if that has not already been done.

### Current Blocker

- Startup now fails with `Schema validation: missing table [flashcard_decks]`.
- `V1__create_flashcard_decks.sql` exists under `src/main/resources/db/migration`, but Flyway did not run before Hibernate schema validation.
- This project uses Spring Boot 4.0.8. The POM currently includes `org.flywaydb:flyway-core` directly, so Spring Boot's Flyway auto-configuration module is not activated.
- The Redis repository-assignment message in the startup output is informational and is not the startup failure.

### Exact Next Step

1. In `pom.xml`, replace `org.flywaydb:flyway-core` with `org.springframework.boot:spring-boot-starter-flyway`.
2. Keep `org.flywaydb:flyway-database-postgresql`.
3. Reload Maven and run `FlashcardGeneratorApplication` again.
4. Confirm Flyway applies `V1__create_flashcard_decks.sql`, Hibernate validation passes, and Tomcat starts on port 8080.
5. After database startup works, finish the remaining AI-output guardrails: allowed difficulty, cognitive level, confidence range, and duplicate detection.

### Planned Work After Database Startup

The next topic is large-document processing and Redis architecture. RAG is intentionally deferred.

Target responsibilities:

- Redis stores only temporary job state: job ID, status, progress, file name, and a short error message.
- Redis must not permanently store the uploaded file, full extracted text, or completed flashcard JSON.
- PostgreSQL stores completed flashcard decks and other durable application records.
- Object storage will eventually hold uploaded PPT/PDF/DOCX files.
- Large presentations will be parsed and divided by slide or token budget, then sent to Gemini in batches. This is basic chunking, not RAG.

Current Redis weaknesses:

- `FlashcardJob` no longer contains `extractedText` or `flashcardsJson`.
- Redis job entries now expire 24 hours after their latest update.
- The result endpoint now retrieves completed decks from PostgreSQL by job ID, so durable results do not depend on Redis.
- `processFlashcards` receives the entire upload as a `byte[]`, so large files remain in JVM memory during processing.
- `@Async` runs work in another application thread but is not a durable job queue; an application restart can lose in-progress work.

Implementation order:

1. Verify Flyway creates `flashcard_decks` and the application starts successfully.
2. Finish deterministic guardrails: allowed difficulty and duplicate questions/answers. Cognitive-level and confidence validation are now implemented and tested.
3. ~~Refactor Redis jobs to contain status/progress metadata only.~~ Completed 2026-10-22.
4. ~~Add a Redis TTL, initially about 24 hours.~~ Completed 2026-10-22.
5. Learn tokens, model context windows, and token budgeting.
6. Implement slide-based or token-budget chunking and process chunks in batches.
7. Add object storage so background processing receives a storage key instead of the complete `byte[]`.
8. Later, replace or augment `@Async` with a durable queue/worker design if production reliability requires it.

Local Redis is already defined in `docker-compose.yml` and can be started with `docker compose up -d redis`. Production should use a managed Redis service configured through the `REDIS_URL` Infisical secret.


## 2026-11-05

### Checkpoint: Text Chunking (Step 6 started)

- Last commit: `0eec741` (2026-10-23) — 24-hour Redis TTL (D-04). Steps 3 and 4 of the implementation order remain complete.
- Uncommitted work: `TextChunkingService` (staged, new) and `TextChunkingServiceTest` (untracked, new).
- `TextChunkingService.chunk(text, maxCharacters)` splits on blank-line paragraph boundaries and packs whole paragraphs into chunks of at most `maxCharacters`. Null or blank text returns an empty list. A non-positive limit throws `IllegalArgumentException`.
- Tests cover blank input, an invalid limit, text within the limit, and multi-paragraph splitting with order preserved. They have not been run in this session.
- The service is not yet wired into `FlashcardService`.

### Known Gaps

- A single paragraph longer than `maxCharacters` is emitted as one oversized chunk. It needs a fallback split on sentence or word boundaries.
- Limits are in characters, not tokens. Token budgeting (step 5) will need to replace or inform the limit.
- Slide-aware chunking for PPT input has not been started.
- The Flyway/Hibernate startup blocker from 2026-10-20 has not been confirmed fixed in this log.

### Next Step

1. Run `mvn test -Dtest=TextChunkingServiceTest`.
2. Add a test and a fallback for oversized paragraphs.
3. Commit the chunking service and its tests.
4. Wire chunking into `FlashcardService`: send chunks to Gemini in batches and merge the validated cards.

### Checkpoint: Text Cleaning Added

- Added `TextCleaningService` (+ `TextCleaningServiceTest`) and wired it into `FlashcardService` right after parsing, before text goes to Gemini.
- Pipeline is now: parse -> clean -> (chunk, not wired yet) -> Gemini -> validate -> save.
- Pre-existing failing test: `FlashcardServiceTest.processFlashcards_Success` expects progress 100 but `FlashcardService` sets 2 (HEAD already does this). Unrelated to cleaning; needs a decision on which value is right.
- Decided: difficulty rules (easy = from document, medium = topic + some document, hard = scenario) go in the Gemini prompt only; no `sourceSnippet`. Add a per-card `topic` tag. Not implemented yet.

## 2026-11-06

### Checkpoint: Chunker Fallback Done

- `TextChunkingService` now splits oversized paragraphs by sentence (`BreakIterator`), then by word, then hard-cut. Tests pass. Not yet wired into `FlashcardService`; nothing committed.
- Pending small decisions: `PptParser` `setMasterByDefault(false)`, slim `TextCleaningService` to regex patterns only, add prompt rule to ignore ads/footers, progress value (2 vs 100) in `FlashcardServiceTest`.

### Enterprise Design Review (revisit later)

Based on code read so far; controller and security config were NOT reviewed, so section 2 items are open questions.

**1. Reliability**
- `@Async` is not durable: a restart leaves jobs stuck in `PROCESSING` until the Redis key expires. Fix: startup sweep marking stale jobs `FAILED` first; durable queue (Redis Streams/SQS/RabbitMQ) plus worker later.
- Full upload held as `byte[]` in memory. Fix: object storage, pass a key.
- Gemini calls lack timeout, retry, and backoff. With chunking, one failed chunk should not fail the job.
- Job state in Redis and deck in PostgreSQL have no single source of truth. Make `saveDeck` idempotent by job ID and order writes so they can be retried.

**2. Security and abuse (verify in controller)**
- File validation by content type, size, and page/slide count; zip-bomb protection (POI `ZipSecureFile`); ideally parse outside the web process.
- Authentication, per-user rate limits, and a daily Gemini cost cap.
- Job IDs must be unguessable and tied to the owning user.
- Prompt injection: delimit document text as data in the prompt; keep structured-output validation.
- Rotate Neon password and Infisical client secret (exposed earlier).

**3. Cost and quality**
- Track tokens and cost per job; cap input size before calling Gemini.
- Cache by file hash.
- Version prompt and model name; store the version on each deck.
- Build a small evaluation set of real documents and re-run it on prompt changes.

**4. Operations**
- Per-job metrics through OpenTelemetry (duration, failures, retries, tokens); job ID in logs; never log document text.
- Health/readiness endpoints for Redis, PostgreSQL, and Gemini.
- Graceful shutdown for in-flight jobs.
- Dead-letter handling for jobs that fail after N retries.

**5. Smaller design issues**
- Split `FlashcardService.processFlashcards` into pipeline steps.
- Make progress meaningful (chunks done / total chunks) and define the contract once.

**Suggested order:** Gemini timeout/retry and per-chunk failure handling while wiring chunking -> file validation -> stuck-job sweep and object storage -> durable queue only if scale requires -> auth, quotas, and cost tracking before any public launch.

### Checkpoint: Single-Call Generation (chunking reverted)

- **Reverted** `TextChunkingService` and its test (never committed). First iteration uses one Gemini call per document. Revisit chunking only if real decks show coverage or output-limit problems.
- Prompt now: AI chooses 5 to 30 cards by content amount; fewer is better than padding; difficulty mix is soft guidance (easy = from document, medium = topic + some document, hard = scenario); junk (ads, copyright, page numbers) ignored; document wrapped in `<document>` tags and treated as data (prompt-injection guard).
- `tags` = document topic labels. `sourceSnippet` kept (required by validator); for hard cards it is the text the scenario is based on. It is not verified against the document.
- `FlashcardValidationService`: difficulty must be easy/medium/hard (case-insensitive, normalized to lowercase); maximum 40 cards per response.
- `TextCleaningService` slimmed to regex patterns only (page numbers, copyright, confidential); company footers and ads left to the prompt.
- Dropped ideas: code-side card-count policy and config settings (unneeded while the AI decides the count).
- Still open: `PptParser` `setMasterByDefault(false)`; progress value (2 vs 100) in `FlashcardServiceTest`; input-size guard (reject cleaned text over about 100,000 characters); possible truncated-JSON handling; nothing committed.
