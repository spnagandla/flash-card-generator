# AI Roadmap

This project already has a basic AI flow:

```text
Document -> Extract text -> Prompt Gemini -> Generate flashcards
```

The next improvements should add AI concepts gradually, in an order that teaches useful patterns without overcomplicating the app.

## Concepts

| Concept | Used For | Project Fit |
|---|---|---|
| Prompt engineering | Controlling model behavior and output quality | Already used |
| Structured output | Getting predictable JSON from the model | Already used |
| Guardrails | Validating and rejecting bad AI output | Add soon |
| Chunking | Splitting large documents into smaller text sections | Add soon |
| RAG | Answering/generating using retrieved document context | High value |
| Embeddings | Turning text into searchable vectors | Needed for semantic RAG |
| Vector database | Storing and searching embeddings | Add with RAG |
| Semantic search | Finding content by meaning, not exact words | Add with RAG |
| Evaluation | Scoring AI output quality | Useful after generation is stable |
| Feedback loop | Improving results from user ratings/corrections | Later |
| Tool calling | Letting AI call backend functions | Later |
| MCP | Standard protocol for exposing app tools/data to AI clients | Later |
| Agents | Multi-step AI workflows using tools and memory | Later |
| Multimodal AI | Using slide images, diagrams, audio, or video | Later |

## Recommended Implementation Order

1. Improve structured flashcard storage.
   - Save flashcards in Firestore as real objects/arrays, not JSON strings.

2. Add output validation.
   - Check valid JSON.
   - Ensure required fields exist.
   - Ensure each card has exactly 3 distractors.
   - Reject or retry invalid Gemini output.

3. Add chunking.
   - Split extracted document text into smaller chunks.
   - Generate cards per chunk.
   - Merge and remove duplicate cards.

4. Add RAG.
   - Store document chunks.
   - Retrieve relevant chunks for a user question or topic.
   - Ask Gemini to answer using only retrieved chunks.

5. Add embeddings and vector search.
   - Generate embeddings for chunks.
   - Store vectors in a vector database.
   - Use semantic search to find relevant chunks.

6. Add an "Ask My Document" feature.
   - User uploads a document.
   - App chunks and stores content.
   - User asks a question.
   - App retrieves relevant chunks.
   - Gemini answers with source-backed context.

7. Add AI evaluation.
   - Score card quality.
   - Detect duplicates.
   - Detect weak or unsupported cards.
   - Use confidence and source snippets to help review.

8. Add a feedback loop.
   - Let users rate cards.
   - Store edits and ratings.
   - Use feedback to improve future generation.

9. Add MCP later.
   - Expose app tools like:
     - `get_flashcard_deck(jobId)`
     - `search_uploaded_documents(query)`
     - `create_flashcards_from_document(documentId)`
     - `save_user_feedback(cardId, rating)`

10. Add agents later.
    - Example workflow:
      - create deck
      - quiz user
      - grade answers
      - identify weak topics
      - generate review cards

## Best Next AI Feature

Build an "Ask My Document" feature after chunking.

Target flow:

```text
User uploads PPT/PDF
App extracts text
App chunks text
App stores chunks
User asks a question
App retrieves relevant chunks
Gemini answers using only those chunks
```

This teaches:

- RAG
- embeddings
- chunking
- retrieval
- grounding
- source citations
- prompt design
