# Spring AI Learning Roadmap

Last updated: November 15, 2026

## Purpose

This file is the restart point for learning Spring AI through the flashcard generator project.

When returning to this topic, begin with **Next Session Starting Point** below. Do not start with RAG, chat memory, or tool calling until the current one-shot flashcard generation flow is observable and reliable.

## Spring AI Already Used

The project is already using Spring AI in `GeminiService` through the Google GenAI starter.

Implemented concepts:

1. Google GenAI model auto-configuration through `spring-ai-starter-model-google-genai`.
2. `ChatClient.Builder` supplied by Spring Boot auto-configuration.
3. The fluent `ChatClient` API.
4. Separate system and user message roles.
5. A synchronous `.call()` to Gemini.
6. Typed structured output with:

   ```java
   .entity(new ParameterizedTypeReference<>() {})
   ```

7. Automatic conversion of Gemini JSON into `List<Flashcard>`.

The following are application/Spring features, not Spring AI features:

- PDF, Word, and PowerPoint parsing
- Redis job state
- Long polling
- PostgreSQL persistence
- `@Async` background processing
- `FlashcardValidationService` business validation

## Configuration Notes

The project currently uses:

```properties
spring.ai.google.genai.api-key=${GEMINI_API_KEY:}
spring.ai.google.genai.chat.model=${GEMINI_MODEL:gemini-3.1-flash-lite}
spring.ai.google.genai.chat.temperature=0.2
spring.ai.model.chat=${SPRING_AI_MODEL_CHAT:none}
```

`SPRING_AI_MODEL_CHAT` must enable the Google GenAI chat model in the runtime environment. If it remains `none`, the auto-configured `ChatClient.Builder` may not be available.

The comment in `application.properties` describing `GeminiService` as manual is outdated. It now uses Spring AI. The separate `gemini.api.key` check is also partly redundant because Spring AI reads `spring.ai.google.genai.api-key`.

## Recommended Learning Sequence

### 1. Capture response metadata and token usage

This is the next feature to implement.

The current `.entity(...)` call returns only `List<Flashcard>`. Learn Spring AI's `responseEntity(...)` API so the application receives both:

- The typed `List<Flashcard>`
- The underlying `ChatResponse`

Use the response metadata to learn and record:

- Input tokens
- Output tokens
- Total tokens
- Model name
- Gemini call duration
- Prompt version

Possible persistence fields for a completed deck:

```text
model_name
prompt_version
input_tokens
output_tokens
total_tokens
generation_duration_ms
```

### 2. Strengthen structured output

The current prompt asks Gemini to return JSON, and Spring AI converts it into Java objects.

Explore structured-output reliability options:

```java
.entity(type, options -> options
    .useProviderStructuredOutput()
    .validateSchema())
```

Verify that the exact overload and Google GenAI provider support work with the project's Spring AI version before implementation.

Keep `FlashcardValidationService`. Schema validation checks JSON structure and types; the validation service checks domain rules such as:

- Exactly three distractors
- Exactly three incorrect explanations
- Allowed difficulty and cognitive-level values
- Confidence range
- Grounding requirements
- Duplicate or unusable cards

### 3. Move prompts into templates

Move the large prompt out of `GeminiService.java` into a resource such as:

```text
src/main/resources/prompts/flashcard-generation.st
```

Learn:

- `PromptTemplate`
- Resource-backed prompts
- Runtime template parameters
- Custom template delimiters
- Prompt versioning
- Separating prompt changes from service orchestration

The document text should be supplied as a parameter rather than inserted with `String.format`.

### 4. Learn model options

Learn global and per-request configuration for:

- Model selection
- Temperature
- Maximum output tokens
- Response format
- Gemini thinking level or thinking budget, depending on model support

Flashcard generation should normally use a low temperature for consistent factual output.

### 5. Add Spring AI observability

The repository already contains OpenTelemetry and SigNoz support. Connect Spring AI observations to the existing monitoring setup.

Measure:

- Chat model duration
- Success and failure counts
- Token usage
- Model name
- Advisor execution
- Per-job generation duration

Do not log full prompts, extracted document text, API keys, or generated private study material in production.

### 6. Introduce Spring AI Document and ETL concepts

Represent extracted content as Spring AI `Document` objects with metadata:

```java
new Document(
    slideText,
    Map.of(
        "fileName", fileName,
        "slideNumber", slideNumber
    )
)
```

Learn the ETL abstractions:

```text
DocumentReader
→ DocumentTransformer
→ DocumentWriter
```

Use `TokenTextSplitter` or another appropriate transformer when real documents exceed a safe model-input size. Chunk by slide or semantic boundary where possible instead of blindly cutting text.

This step does not require RAG.

### 7. Add AI evaluation tests

Create a small repeatable evaluation set containing real documents and expected important concepts.

Explore:

- `Evaluator`
- `RelevancyEvaluator`
- `FactCheckingEvaluator`
- Custom evaluation prompts

Evaluate whether generated flashcards are:

- Supported by the source document
- Relevant to important document concepts
- Non-duplicative
- Balanced across difficulty levels
- Free of obvious answer-length clues

Do not run expensive model-based evaluation as part of every ordinary unit-test run. Keep it as a separate integration/evaluation test suite.

## Features To Postpone

### RAG and vector stores

Use RAG only when the product needs features such as:

- Asking questions across many uploaded documents
- Retrieving relevant passages from very large documents
- Searching a user's deck library semantically
- Generating cards from selected topics across multiple sources

RAG would introduce embeddings, a vector store, similarity search, retrieval filters, and retrieval advisors. It is unnecessary for a single document that fits safely in Gemini's context window.

PostgreSQL with PGvector would be a natural future option because the project already uses PostgreSQL.

### Chat memory

Use chat memory only after adding a conversational tutor. One-shot deck generation does not need conversation history.

### Tool calling

Use tool calling when an AI tutor genuinely needs to invoke application functions, such as:

- Loading a particular deck
- Retrieving a user's score
- Scheduling a review session
- Looking up current application data

Tool calling is not needed for the current document-to-flashcards pipeline.

### Streaming

Streaming is useful for visible text generation, but the current application needs a complete typed `List<Flashcard>`. Structured `.entity(...)` conversion requires the complete response, so streaming is not the next priority.

### Multimodal input

Consider this later for slides containing important diagrams, charts, and screenshots that text extraction cannot represent. It may require rendering slides as images and using a Gemini model that supports multimodal input.

## Next Session Starting Point

Start here:

1. Re-read `GeminiService.java` lines containing the `ChatClient` call.
2. Read the Spring AI structured-output documentation for `responseEntity(...)`.
3. Change the call so it returns typed flashcards plus the underlying `ChatResponse`.
4. Inspect token-usage metadata in a test or debugger.
5. Add a small application record for generation metadata.
6. Decide which metadata should be persisted on `FlashcardDeck`.
7. Add tests before changing the database migration.

Do not implement prompt templates, chunking, or RAG in the same change. Finish and verify response metadata first.

## Reference Documentation

- Spring AI introduction: https://docs.spring.io/spring-ai/reference/
- ChatClient: https://docs.spring.io/spring-ai/reference/api/chatclient.html
- Structured output: https://docs.spring.io/spring-ai/reference/api/structured-output.html
- Prompts and templates: https://docs.spring.io/spring-ai/reference/api/prompt.html
- Google GenAI chat: https://docs.spring.io/spring-ai/reference/api/chat/google-genai-chat.html
- Observability: https://docs.spring.io/spring-ai/reference/1.0/observability/index.html
- ETL pipeline: https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html
- Evaluation testing: https://docs.spring.io/spring-ai/reference/api/testing.html
- RAG: https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html
- Tool calling: https://docs.spring.io/spring-ai/reference/api/tools.html
- Chat memory: https://docs.spring.io/spring-ai/reference/2.1/api/chat-memory.html

