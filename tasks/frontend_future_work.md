# Frontend Future Work

Last updated: November 15, 2026

## Purpose

This file is the restart point for future React UI work on the flashcard generator.

When frontend work resumes, read this file, `tasks/react-ui-progress-2026-06-12.md`, and the current files under `frontend/src/` before making changes.

## Current Frontend State

Implemented:

- File picker and drag-and-drop upload area
- PDF, Word, and PowerPoint extension validation
- 25 MB client-side file-size validation
- Matching 25 MB Spring file limit
- Selected filename and readable file size
- Remove-file button that returns to the empty state
- Generate button with upload and processing states
- Upload and processing controls disabled while a job is active
- Multipart upload to `POST /api/flashcards/upload`
- Returned `jobId` stored in React state
- True long polling through `GET /api/flashcards/status/{jobId}/wait`
- `PENDING`, `PROCESSING`, `COMPLETED`, and `FAILED` status UI
- Indeterminate progress display because the backend does not provide a real percentage
- Long-poll timeout reconnection and network-error retry
- `AbortController` cleanup
- Success and error toasts
- Toast auto-dismiss and manual close button
- Introductory Create Flashcards button hidden after opening the upload area
- Consistent button capitalization

Not yet implemented:

- Fetching the generated result
- React routing
- Dedicated deck page
- Interactive questions and answers
- Numbered card navigation
- Score tracking
- Final summary
- Persistent recent jobs/decks

## Recommended User Flow

```text
Upload page
  → Select document
  → Generate Flashcards
  → Backend processing status
  → COMPLETED
  → Show result summary and Study Flashcards button
  → User chooses Study Flashcards
  → Navigate to /decks/{jobId}
  → Interactive study session
```

Do not automatically redirect as soon as processing completes. Display a clear completion state and let the user choose when to open the deck:

```text
✓ 15 flashcards are ready

[Study Flashcards]
```

This avoids unexpectedly moving the user if background processing finishes while they are doing something else.

## Recommended Routes

```text
/                 Upload and recent jobs/decks
/jobs/{jobId}     Optional dedicated job-status page
/decks/{jobId}    Interactive flashcard study page
```

Use a route inside the React single-page application. Do not open a separate browser tab.

The deck route must be refresh-safe. When `/decks/{jobId}` loads, fetch the deck from:

```text
GET /api/flashcards/result/{jobId}
```

The result is stored in PostgreSQL, so it can remain available after the temporary Redis job-status entry expires.

## First Frontend Checkpoint

Implement result fetching before routing or card interactions.

When the status becomes `COMPLETED`:

1. Call `GET /api/flashcards/result/{jobId}`.
2. Store the result in typed React state.
3. Handle result loading and errors.
4. Display the filename and number of generated cards.
5. Display a Study Flashcards button.

Initial success UI:

```text
✓ chapter-5.pptx is ready
15 flashcards generated

[Study Flashcards]
```

Suggested state:

```tsx
const [flashcardResult, setFlashcardResult] =
    useState<FlashcardResultResponse | null>(null)

const [isLoadingResult, setIsLoadingResult] =
    useState(false)

const [resultError, setResultError] =
    useState<string | null>(null)
```

Use `AbortController` for the result request cleanup.

## Suggested Component Structure

Keep `App.tsx` focused on page-level orchestration. Extract study components rather than building the whole deck inside `App.tsx`.

```text
App
├── UploadArea
├── JobStatus
├── Toast
└── Routes
    └── DeckPage
        ├── DeckHeader
        ├── CardNavigator
        ├── StudyProgress
        ├── FlashcardQuestion
        ├── AnswerOptions
        ├── AnswerFeedback
        └── DeckSummary
```

Possible folders:

```text
frontend/src/
├── api/
│   └── flashcards.ts
├── components/
│   ├── CardNavigator.tsx
│   ├── FlashcardQuestion.tsx
│   ├── AnswerOptions.tsx
│   └── AnswerFeedback.tsx
├── pages/
│   └── DeckPage.tsx
├── types/
│   └── flashcards.ts
└── App.tsx
```

Do not create all files at once. Extract components as each checkpoint becomes functional.

## Interactive Deck Design

Show one flashcard at a time.

```text
Chapter 5                              Score: 3
Card 4 of 15                           [progress]

What does dependency injection accomplish?

[Option A]
[Option B]
[Option C]
[Option D]

[Previous]                              [Next]
```

After the user selects an answer, lock that card's answer and show feedback.

Correct answer example:

```text
✓ Correct

Explanation:
Dependency injection supplies dependencies externally...

Source:
“Spring supplies collaborating objects...”
```

Incorrect answer example:

```text
✕ Incorrect

Your answer: Option B
Correct answer: Option D

Why your answer was wrong:
...

Main explanation:
...
```

## Building Answer Options

The backend returns:

```text
answer
distractors[3]
incorrectExplanations[3]
```

Build the four options with:

```tsx
const options = [
    flashcard.answer,
    ...flashcard.distractors,
]
```

Shuffle the options once per card. Do not shuffle during every render, or answer buttons can move after the user clicks one.

Preserve the relationship between each distractor and its incorrect explanation while shuffling. A useful option model is:

```tsx
type AnswerOption = {
    text: string
    isCorrect: boolean
    incorrectExplanation: string | null
}
```

## Numbered Card Navigator

Users must be able to jump directly to any question, such as question 20.

Desktop example:

```text
Questions

[1] [2] [3] [4] [5]
[6] [7] [8] [9] [10]
[11] [12] [13] [14] [15]
[16] [17] [18] [19] [20]
```

Clicking question 20 sets:

```tsx
setCurrentCardIndex(19)
```

Arrays use zero-based indexes:

```text
Question 1  → index 0
Question 20 → index 19
```

Each number should display its state:

```text
Purple outline  Current card
Green/check     Answered correctly
Red/x           Answered incorrectly
Gray/empty      Not answered
```

On mobile, use a horizontally scrolling navigator:

```text
← 17 18 19 [20] 21 22 →
```

Support both:

- Previous and Next for sequential study
- Numbered navigation for direct access

Consider reflecting the current card in the URL:

```text
/decks/{jobId}?card=20
```

That lets refresh preserve the current position.

## Store Answers Per Card

A single `selectedAnswer` state is insufficient because users can jump between cards.

Store an answer record for each card:

```tsx
type AnswerRecord = {
    selectedAnswer: string
    isCorrect: boolean
}

const [answersByCard, setAnswersByCard] =
    useState<Record<number, AnswerRecord>>({})
```

Examples:

```text
answersByCard[0]  → answer for question 1
answersByCard[19] → answer for question 20
```

When revisiting an answered card, restore:

- Selected option
- Correct/incorrect styling
- Explanation
- Source snippet

Do not increment the score again when revisiting an already answered card.

## Study Session State

Likely state for the first interactive version:

```tsx
const [currentCardIndex, setCurrentCardIndex] = useState(0)
const [answersByCard, setAnswersByCard] =
    useState<Record<number, AnswerRecord>>({})
```

Derive the score instead of maintaining a separate counter when practical:

```tsx
const score = Object.values(answersByCard)
    .filter(answer => answer.isCorrect)
    .length
```

Derived score avoids accidental double counting.

## Final Summary

After all cards are answered, show:

```text
Deck complete

Score: 23 / 30
Accuracy: 77%

[Review Incorrect Cards]
[Restart Deck]
[Return to My Decks]
```

Useful later improvements:

- Review only incorrect cards
- Review only unanswered cards
- Restart while preserving the original deck
- Track per-topic performance
- Track difficulty-level performance
- Persist study attempts in PostgreSQL

## Accessibility Requirements

- Every answer must be a real `<button>`.
- Numbered card navigation must use buttons with descriptive `aria-label` values.
- Indicate current card using `aria-current`.
- Do not communicate correct/incorrect state using color alone.
- Move keyboard focus to the question after navigation.
- Support keyboard answer selection, optionally keys 1 through 4.
- Announce answer feedback using an appropriate live region.
- Ensure Previous, Next, and numbered navigation have visible focus states.

## Error and Recovery Behavior

- If result fetching fails temporarily, show Retry.
- If result returns `404`, explain that the deck could not be found.
- If result returns `409`, the job is not completed; return to status monitoring.
- A page refresh on `/decks/{jobId}` must fetch from PostgreSQL again.
- Do not depend on the Redis job key after the deck has been persisted.
- Later, associate decks with authenticated users instead of relying only on possession of a job ID.

## Implementation Order

Follow this order and verify each checkpoint before continuing:

1. Define frontend `Flashcard` and `FlashcardResultResponse` types.
2. Fetch the result after `COMPLETED`.
3. Display filename and card count.
4. Add Study Flashcards button.
5. Add React routing and `/decks/{jobId}`.
6. Make the deck route fetch its result on page load.
7. Render one question without interactions.
8. Build and shuffle four answer options once per card.
9. Add per-card answer state and feedback.
10. Add Previous and Next navigation.
11. Add the numbered `CardNavigator`.
12. Add score and progress indicators.
13. Add final summary and review-incorrect flow.
14. Add accessibility checks and component tests.
15. Add recent decks and persisted study attempts later.

## Exact Next Session Starting Point

Start with only this vertical slice:

```text
Job becomes COMPLETED
→ fetch /api/flashcards/result/{jobId}
→ store typed result
→ display filename and number of cards
→ display Study Flashcards button
```

Do not add routing, answer selection, scoring, or the numbered navigator until this result-fetching slice works and is tested.

