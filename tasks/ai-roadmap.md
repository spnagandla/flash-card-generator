# Production AI Engineering Roadmap

Last updated: June 13, 2026

## Goal

Turn the current flashcard generator into a production-quality AI learning platform while learning the complete AI application stack in dependency order.

This roadmap covers:

- LLM and token fundamentals
- prompt and context engineering
- structured output and guardrails
- evaluation and observability
- chunking, embeddings, vector search, and RAG
- memory, tool calling, workflows, agents, and multi-agent systems
- Spring AI, LangChain, LangGraph, skills, and MCP
- security, privacy, cost, reliability, deployment, and operations

## Current System

```text
Document
  -> extract text
  -> build prompt
  -> call Gemini
  -> receive raw JSON text
  -> save result
```

Current strengths:

- Working document parsers
- Working Gemini REST call
- Prompt requests a structured flashcard schema
- Redis-backed asynchronous jobs
- Firebase persistence
- React upload flow

Current critical gap:

- Gemini output is trusted as a raw string instead of parsed, validated domain data.

## Framework Strategy

### Primary Stack: Java And Spring AI

Keep Spring Boot as the production application.

Use Spring AI when framework adoption becomes useful for:

- model-provider abstraction
- structured output mapping
- embeddings
- vector stores
- RAG advisors
- tool calling
- chat memory
- evaluation
- observability
- MCP clients and servers

Do not migrate immediately. First implement and understand the underlying concepts using the current Gemini integration, then decide which custom code Spring AI should replace.

### LangChain

Learn LangChain later as a comparative framework for model, tool, retrieval, middleware, and agent abstractions.

It is mainly a Python/JavaScript ecosystem, so using it directly would introduce a second runtime or a separate AI service. Do not rewrite the Java application only to claim framework usage.

### LangGraph

Use LangGraph only when the application needs a long-running, stateful workflow with:

- multiple branching steps
- persistence and resumability
- human approval
- retries and recovery
- explicit agent state
- tool loops or subagents

A single Gemini call or basic RAG request does not require LangGraph.

## Phase 0: AI, Machine Learning, And LLM Foundations

Goal:

Understand how learning systems differ from normal software and how modern LLMs are created, without requiring advanced mathematics or training a foundation model from scratch.

Learn:

- artificial intelligence versus machine learning versus deep learning versus generative AI
- deterministic programs versus learned models
- datasets, examples, features, labels, and targets
- training versus inference
- supervised learning
- unsupervised learning
- self-supervised learning
- reinforcement learning
- training, validation, and test splits
- loss functions and optimization at a conceptual level
- gradient descent and backpropagation at a conceptual level
- underfitting, overfitting, generalization, and data leakage
- classification, regression, clustering, and dimensionality reduction
- neural-network layers, weights, activations, and parameters
- transformer, attention, and next-token prediction concepts
- pretraining, instruction tuning, fine-tuning, preference tuning, and RLHF/RLAIF
- hallucination, probabilistic output, and knowledge cutoffs
- embeddings and vector databases at a conceptual level

Vector database mental model:

```text
Text or document chunk
  -> embedding model converts meaning into a numeric vector
  -> vector database stores the vector plus metadata

User question
  -> converted into a vector
  -> nearest vectors are retrieved by semantic similarity
  -> matching document chunks become context for the LLM
```

A vector database retrieves semantically related data. It does not generate the final answer.

Practice:

- identify supervised, unsupervised, self-supervised, and reinforcement-learning examples
- train one small classification model using a learning library or notebook
- compare training and inference
- inspect overfitting using training and validation results
- create a tiny embedding similarity demonstration

Exit criteria:

- explain how normal code differs from a trained model
- explain how an LLM is pretrained and later aligned for instruction following
- explain why an LLM can hallucinate
- explain what embeddings and vector databases do in RAG
- understand enough theory to make sound application-engineering decisions

Not required before continuing:

- advanced calculus
- deriving backpropagation equations
- implementing a transformer from scratch
- training a foundation model

## Phase 1: Production Baseline

Learn:

- deterministic code versus probabilistic model behavior
- environment configuration and secret management
- request IDs, job IDs, and correlation IDs
- timeouts, retries, backoff, and idempotency
- rate limits and quotas
- unit, integration, and end-to-end tests

Implement:

- server-side file type and size validation
- consistent 20 MB or 25 MB limits
- Gemini connection/read timeouts
- classified API errors
- safe retry policy for transient failures
- Firebase exception propagation
- structured logs without document text or secrets
- health checks and production configuration profiles

Exit criteria:

- predictable failure states
- no silent persistence failures
- no unbounded external requests

## Phase 2: LLM And Token Foundations

Learn:

- transformer and LLM concepts at an application-engineering level
- tokens and tokenization
- input, output, and reasoning tokens
- context windows
- sampling parameters: temperature, top-p, and maximum output tokens
- latency, throughput, rate limits, and cost
- model selection and model/version lifecycle
- hallucination and nondeterminism

Implement:

- count or estimate tokens before Gemini requests
- capture usage metadata after requests
- enforce an input token budget
- enforce an output token budget
- record latency and estimated cost per generation
- define model configuration outside application code

Exit criteria:

- large documents cannot accidentally create uncontrolled requests
- token usage and latency are measurable

## Phase 3: Prompt And Context Engineering

Learn:

- system instructions versus user content
- zero-shot and few-shot prompting
- delimiters and clear task boundaries
- positive instructions and explicit constraints
- context engineering
- context selection, ordering, compression, and isolation
- context rot and irrelevant-context problems
- prompt versioning
- prompt injection from uploaded documents

Implement:

- separate prompt template from `GeminiService`
- version prompts
- clearly delimit untrusted document text
- add one or two examples only when evaluation proves value
- store prompt/model version with every generated deck
- create a context-builder abstraction

Exit criteria:

- prompts are testable and versioned
- document text cannot be confused with system instructions

## Phase 4: Structured Output And Guardrails

Learn:

- JSON parsing versus JSON schema
- typed structured output
- syntactic, semantic, and business-rule validation
- input guardrails and output guardrails
- repair and bounded retry strategies
- fail-open versus fail-closed decisions

Implement first:

```text
Gemini response
  -> parse List<Flashcard>
  -> validate every field
  -> reject invalid output
  -> persist only validated cards
```

Validate:

- required nonblank fields
- exactly three distractors
- matching incorrect explanations
- allowed difficulty values
- allowed cognitive levels
- confidence from 0.0 to 1.0
- one to three valid tags
- supported source evidence
- duplicate questions and answers

Then:

- request provider-native structured output/schema when supported
- add one controlled repair or regeneration attempt
- store validation failures for analysis

Exit criteria:

- malformed model output never becomes a completed deck
- validation behavior has automated tests

## Phase 5: Evaluation-Driven Development

Evaluation starts before RAG and continues through every later phase.

Learn:

- offline versus online evaluation
- golden datasets
- deterministic metrics
- semantic metrics
- LLM-as-a-judge and its limitations
- human review
- regression testing
- A/B testing

Create a representative dataset containing:

- short and long PDFs
- Word and PowerPoint documents
- tables, headings, repeated text, and noisy text
- easy, medium, and difficult subjects
- malicious prompt-injection documents

Measure:

- schema validity
- factual support from source text
- distractor quality
- duplicate rate
- coverage of important concepts
- difficulty calibration
- source-snippet correctness
- latency, token usage, and cost

Exit criteria:

- prompt or model changes cannot ship without evaluation
- quality has measurable baselines and thresholds

## Phase 6: Document Intelligence And Chunking

Learn:

- parsing versus document understanding
- normalization and metadata extraction
- token-aware chunking
- fixed, recursive, semantic, and structure-aware chunking
- overlap tradeoffs
- parent-child chunks
- chunk provenance

Implement:

- normalize extracted text
- preserve page, slide, heading, and section metadata
- chunk by document structure where possible
- enforce token-sized chunks
- generate cards per chunk
- merge results
- remove near-duplicate cards
- preserve source references

Exit criteria:

- large documents fit token budgets
- every generated card can point to source metadata

## Phase 7: Embeddings And Vector Search

Learn:

- embedding vectors
- semantic similarity
- cosine similarity, dot product, and distance
- vector dimensions
- indexing and approximate nearest-neighbor search
- metadata filters
- embedding model/version migrations

Implement:

- select an embedding model
- embed chunks
- use a vector store such as PostgreSQL with pgvector unless requirements justify another database
- store document, user, page, section, and model-version metadata
- implement top-k semantic search
- combine semantic retrieval with metadata filters

Exit criteria:

- relevant source chunks can be retrieved by meaning
- retrieval is tenant/user isolated

## Phase 8: Production RAG

Learn:

- ingestion and retrieval pipelines
- query rewriting
- hybrid search
- reranking
- context packing
- grounding and citations
- retrieval failures versus generation failures
- RAG evaluation

Implement:

```text
Question
  -> authorize document access
  -> rewrite query when useful
  -> retrieve candidate chunks
  -> apply filters
  -> rerank
  -> build token-budgeted context
  -> answer only from retrieved evidence
  -> return citations
```

Build:

- Ask My Document
- topic-focused flashcard generation
- source-backed answers
- abstention when evidence is insufficient

Evaluate:

- retrieval precision and recall
- context relevance
- answer faithfulness
- citation correctness
- answer completeness

Exit criteria:

- answers are grounded, cited, access-controlled, and evaluated

## Phase 9: Memory And Personalization

Learn:

- application state versus model context
- short-term versus long-term memory
- episodic, semantic, and procedural memory
- summarization and forgetting policies
- privacy boundaries

Implement:

- session history
- user learning preferences
- weak-topic tracking
- spaced-repetition data
- explicit memory controls
- retention and deletion policies

Do not:

- send unlimited conversation history
- treat Redis or a database as model memory without retrieval rules

Exit criteria:

- memory is scoped, inspectable, erasable, and token-budgeted

## Phase 10: Tool Calling And Deterministic Workflows

Learn:

- tools/function calling
- tool schemas
- validation and authorization
- deterministic orchestration
- workflow state machines
- compensating actions and idempotency

Potential tools:

- `get_flashcard_deck`
- `search_document_chunks`
- `save_user_feedback`
- `schedule_review`
- `create_flashcards_for_topic`
- `get_learning_progress`

Implement:

- typed, narrow tools
- authentication and authorization per tool
- argument validation
- timeouts and audit logs
- deterministic workflow before agent autonomy

Exit criteria:

- every tool can be tested without an LLM
- the model cannot bypass application authorization

## Phase 11: Agents

Learn:

- workflow versus agent
- planning and tool-use loops
- state, memory, and stopping conditions
- reflection and evaluator-optimizer patterns
- human-in-the-loop
- single-agent versus multi-agent tradeoffs

First useful agent:

```text
Assess learner
  -> identify weak topics
  -> retrieve supporting material
  -> generate review cards
  -> quiz learner
  -> grade answers
  -> update study plan
```

Production controls:

- maximum steps
- maximum tokens and cost
- tool allowlists
- per-tool permissions
- loop detection
- checkpoints
- human approval for consequential actions
- full traces

Exit criteria:

- bounded, observable behavior beats the deterministic baseline

## Phase 12: LangChain And LangGraph

Learn LangChain by rebuilding a small existing capability:

- model abstraction
- structured output
- retriever
- tool
- simple agent
- middleware and context engineering

Learn LangGraph after that:

- state schema
- nodes and edges
- conditional routing
- persistence/checkpoints
- interrupts and human approval
- retries and durable execution
- subgraphs

Architecture options:

1. Keep Spring Boot as the system of record and use a separate Python agent service.
2. Use LangGraph only for advanced learning-agent orchestration.
3. Keep straightforward RAG and generation in Spring AI.

Do not introduce both Spring AI and LangChain abstractions into the same simple request path without a clear boundary.

## Phase 13: Skills And Reusable Capabilities

Treat a skill as a reusable package containing:

- purpose and activation criteria
- instructions or policy
- required context
- allowed tools
- input/output schema
- examples
- tests and evaluation cases
- version and ownership

Project skills could include:

- flashcard generation
- document question answering
- quiz grading
- weak-topic diagnosis
- study-plan generation

Skills should compose tested prompts, tools, retrieval, and policies. They are not a substitute for those foundations.

## Phase 14: MCP

Learn:

- MCP hosts, clients, and servers
- tools, resources, and prompts
- capability discovery
- transports
- authentication and authorization
- trust boundaries and tool poisoning risks

Expose Spring services only after internal tool contracts are stable:

- `get_flashcard_deck(jobId)`
- `search_uploaded_documents(query)`
- `create_flashcards_from_document(documentId)`
- `save_user_feedback(cardId, rating)`

Use Spring AI's MCP support if it fits the production architecture.

Exit criteria:

- least-privilege tools
- user-scoped access
- audit logs
- explicit approval for writes or destructive actions

## Phase 15: Multimodal AI

Learn:

- native document, image, audio, and video inputs
- OCR and layout understanding
- modality-specific token costs
- slide and diagram grounding

Implement:

- extract slide images and speaker notes
- understand diagrams and charts
- generate image-supported flashcards
- add lecture audio transcription
- cite page, slide, image, or timestamp

## Phase 16: Security And Responsible AI

This work runs across every phase.

Cover:

- direct and indirect prompt injection
- insecure output handling
- sensitive information disclosure
- excessive agency
- tool and MCP poisoning
- data exfiltration
- denial-of-wallet and resource exhaustion
- model and dependency supply-chain risks
- tenant isolation
- copyright, retention, deletion, and consent
- abuse monitoring and moderation

Controls:

- treat uploaded text and retrieved content as untrusted
- isolate instructions from data
- sanitize rendered output
- authorize every data and tool operation
- apply quotas and budgets
- encrypt data and secrets
- redact logs
- maintain audit trails

## Phase 17: Observability, Reliability, And Cost

Measure:

- request and job traces
- model, prompt, embedding, and index versions
- token usage and cost
- latency by pipeline stage
- retries, timeouts, and failures
- retrieval results and scores
- validation failures
- agent steps and tool calls
- user feedback and quality metrics

Implement:

- structured logging
- metrics and dashboards
- distributed tracing
- alerting and SLOs
- fallback models
- circuit breakers
- queues and backpressure
- caching and context caching
- cost budgets and per-user quotas

## Phase 18: Production Delivery

Implement:

- authentication and authorization
- user/document ownership
- database migrations
- Docker images
- CI/CD
- dev, staging, and production environments
- automated tests and evaluation gates
- secret rotation
- backups and disaster recovery
- canary or gradual releases
- rollback strategy
- data retention and deletion
- operational runbooks

Production readiness requires:

- functionality
- measured quality
- security
- reliability
- observability
- cost control
- supportable operations

## Correct Learning Order

```text
AI, ML, and LLM foundations
-> production baseline
-> tokens and model fundamentals
-> prompt and context engineering
-> structured output and guardrails
-> evaluation
-> chunking and document intelligence
-> embeddings and vector search
-> production RAG
-> memory and personalization
-> tool calling and workflows
-> agents
-> LangChain and LangGraph
-> skills
-> MCP
-> multimodal features
-> continuous security, observability, and production operations
```

## Immediate Next Step

Current phase: **Phase 0 - AI, Machine Learning, And LLM Foundations**.

Start with:

```text
AI versus ML versus deep learning versus generative AI
  -> datasets, features, and labels
  -> supervised versus unsupervised learning
  -> training, validation, testing, and inference
  -> overfitting and generalization
  -> neural networks and transformers conceptually
  -> embeddings and vector database mental model
```

After Phase 0, continue to the production baseline and token foundations. Typed Gemini output validation is now Phase 4.
