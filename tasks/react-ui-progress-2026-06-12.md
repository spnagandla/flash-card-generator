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

1. Poll `GET /api/flashcards/status/{jobId}`.
2. Display `PENDING`, `PROCESSING`, `COMPLETED`, and `FAILED` states.
3. Fetch `GET /api/flashcards/result/{jobId}` after completion.
4. Build the interactive flashcard deck UI.
5. Add retry, cancel, replace, and remove-file interactions.
6. Add accessibility review for keyboard focus and screen readers.
7. Add component and request tests.
8. Enforce a frontend file-size limit.
9. Reconcile the UI's displayed 25 MB limit with Spring Boot's current 20 MB multipart limit.
10. Move API calls into a small API/service module when frontend work resumes.

## Verification At Pause Point

- `npm run build` passes.
- `npm run lint` passes.
- Backend tests were not run because the repository does not contain `mvnw.cmd`.

## Resume Instruction

When returning to frontend work, read this file and inspect the current files under `frontend/src/` before making changes.
