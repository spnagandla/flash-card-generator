# Daily Memory - 2026-06-26

## Session Focus

We reviewed where the project left off and clarified the current frontend upload flow.

Frontend upload is currently connected end to end:

```text
User selects a document
  -> React stores the browser File in selectedFile
  -> user clicks Generate flashcards
  -> React creates FormData
  -> file is appended using multipart field name "file"
  -> React POSTs /api/flashcards/upload
  -> Vite proxies /api to Spring Boot on localhost:8080
  -> Spring receives @RequestParam("file") MultipartFile file
  -> JobService creates a Redis job with PENDING status
  -> FlashcardService starts async background processing
  -> Spring returns { jobId }
  -> React stores and displays the jobId
```

## Concepts Explained

- React props are data or functions passed from a parent component to a child component.
- `App` is the parent and owns important state like `selectedFile`, `isUploading`, `jobId`, and `toast`.
- `UploadArea` is the child and receives props from `App`.
- Props go down from parent to child.
- Events go back up when the child calls functions passed as props.
- `UploadArea` does not own the selected file because `App` needs the file later for validation, upload, and job tracking.
- Redux would only become useful later if many components need shared state such as `jobStatus`, `flashcards`, `currentCardIndex`, selected answers, score, and review summary.
- For the current app, `useState` plus props is still the right level of complexity.

## Next Frontend Starting Point

Build the status-check UI after upload.

Recommended first step:

1. Create `frontend/src/StatusPanel.tsx`.
2. Add TypeScript types for job status:
   - `PENDING`
   - `PROCESSING`
   - `COMPLETED`
   - `FAILED`
3. Have `StatusPanel` receive the current job status as a prop.
4. Display the uploaded file name, current status, and any error message.
5. Wire `StatusPanel` into `App.tsx`.
6. Add polling with `useEffect` after `jobId` is set.
7. Poll `GET /api/flashcards/status/{jobId}` every few seconds.
8. Stop polling when status is `COMPLETED` or `FAILED`.

Start with the UI/component first, then add polling.

## Status Check Flow

```text
Upload succeeds
  -> backend returns jobId
  -> React stores jobId
  -> React starts polling /api/flashcards/status/{jobId}
  -> status panel shows PENDING or PROCESSING
  -> polling stops when status becomes COMPLETED or FAILED
```

## Starter Type Shape

```tsx
type JobStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'

type JobStatusResponse = {
    jobId: string
    status: JobStatus
    fileName: string
    error: string | null
}

type StatusPanelProps = {
    status: JobStatusResponse | null
}
```

## Reminder

The project learning agreement still applies:

- Explain the concept and data flow before code.
- Keep the user writing learning-focused application code unless they explicitly ask Codex to implement.
- Inspect the actual files before continuing.
