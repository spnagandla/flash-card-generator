# React UI Progress And Handoff

Last updated: June 13, 2026

## Status

Frontend work is paused intentionally. The current focus is learning and implementing the AI pipeline.

Do not restart or redesign the frontend unless the user asks to resume UI work.

## Learning Agreement

- The user normally writes learning-focused application code.
- Codex teaches and reviews unless the user explicitly asks Codex to implement.
- Explain the problem, purpose, data flow, and new syntax before code.
- Teach at a beginner-friendly pace and use Java comparisons when useful.
- Inspect the actual files before deciding what is complete.

## Implemented Frontend

- React + TypeScript application created with Vite
- Responsive full-screen dark gradient layout
- Modern upload panel with drag-active feedback
- Native file picker and drag-and-drop selection
- Selected filename, readable file size, and ready status
- Extension validation for PDF, Word, and PowerPoint
- Reusable `UploadArea` child component with typed props
- Reusable success/error `Toast` component
- Four-second toast timer using `useEffect` with cleanup
- Generate Flashcards button
- Loading, disabled-button, and spinner states
- Multipart upload using `FormData`
- `POST /api/flashcards/upload` using `fetch`
- Vite `/api` proxy to Spring Boot on `http://localhost:8080`
- Backend `{ jobId }` response stored and displayed

Relevant files:

- `frontend/src/App.tsx`
- `frontend/src/UploadArea.tsx`
- `frontend/src/Toast.tsx`
- `frontend/src/App.css`
- `frontend/src/index.css`
- `frontend/vite.config.ts`

## Current Frontend Flow

```text
User opens upload area
  -> selects or drops a supported document
  -> React stores the browser File
  -> Generate Flashcards button appears

User clicks Generate Flashcards
  -> React creates FormData
  -> file is appended using multipart field name "file"
  -> POST /api/flashcards/upload
  -> Vite proxies the request to Spring Boot
  -> Spring returns { jobId }
  -> React stores and displays the jobId
```

## Frontend Work For Later

Latest daily handoff:

- `tasks/daily-memory-2026-06-26.md`

Tomorrow's frontend starting point from that handoff: build the status-check UI after upload.

1. Poll `GET /api/flashcards/status/{jobId}` after upload.
2. Display `PENDING`, `PROCESSING`, `COMPLETED`, and `FAILED` states.
3. Fetch `GET /api/flashcards/result/{jobId}` after completion.
4. Render the generated flashcard deck.
5. Build four answer options from `answer + distractors`.
6. Shuffle options so the correct answer is not always first.
7. Let the user select one option.
8. Show correct or incorrect feedback.
9. If wrong, show the correct answer and the selected distractor's explanation.
10. Show the main explanation and source snippet.
11. Add next/previous card navigation.
12. Track score and show a final summary.

Additional frontend work after that:

- Add retry, cancel, replace, and remove-file interactions.
- Add accessibility review for keyboard focus and screen readers.
- Add component and request tests.
- Enforce a frontend file-size limit.
- Reconcile the UI's displayed 25 MB limit with Spring Boot's current 20 MB multipart limit.
- Move API calls into a small API/service module when frontend work resumes.

Planned flashcard interaction flow:

```text
Upload file
  -> backend returns jobId
  -> frontend polls job status
  -> status becomes COMPLETED
  -> frontend fetches result
  -> user studies one card at a time
  -> user selects one of four options
  -> UI shows correct/incorrect feedback
  -> user moves to next card
  -> final score summary appears
```

Flashcard option model:

```text
options = [answer, ...distractors]
```

The backend already returns `answer` and exactly three `distractors` in the intended schema. The frontend needs to shuffle and display those four options.

## Verification At Pause Point

- `npm run build` passes.
- `npm run lint` passes.
- Backend tests were not run because the repository does not contain `mvnw.cmd`.

## Resume Instruction

When returning to frontend work, read this file and inspect the current files under `frontend/src/` before making changes.
