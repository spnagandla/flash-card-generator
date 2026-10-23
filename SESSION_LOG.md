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

