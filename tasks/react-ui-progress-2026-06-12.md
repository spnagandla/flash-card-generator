# React UI Learning Progress

Date: June 12, 2026

## Learning Agreement

- The user writes the application code.
- Codex guides and reviews; do not edit frontend code unless explicitly asked.
- Teach from a beginner perspective and connect concepts to Java when useful.
- Before giving code, explain:
  1. What problem are we solving?
  2. Why does the UI need this?
  3. How does the proposed code solve it?
  4. What does each new syntax element mean?
- Move one small step at a time. Do not introduce unexplained React concepts.

## Phase 1 Goal

Learn React and TypeScript fundamentals while building a polished, modern flashcard generator UI.

Planned progression:

1. JSX and component structure
2. CSS styling and responsive layout
3. React events
4. React state and conditional rendering
5. File selection
6. Drag and drop
7. TypeScript props and reusable components
8. Loading, success, and error states
9. Spring Boot API integration
10. Flashcard deck interactions and animations

## Concepts Covered

- Frontend folder structure and the roles of `main.tsx`, `App.tsx`, and CSS files
- Why the Vite starter content was replaced
- A React function component: `function App()`
- JSX/TSX versus HTML
- Semantic elements such as `main` and `section`
- Components as reusable UI and behavior, not necessarily one visual box
- `className` and how JSX connects to CSS selectors
- Full-browser sizing through `body`, `#root`, and `.app`
- Basic modern CSS:
  - gradients
  - glass-style card
  - spacing
  - responsive `clamp()`
  - hover and active button states
  - transitions
- React click events with `onClick`
- React state with `useState`
- Conditional rendering with `showUpload && (...)`
- Why a `label` can activate a nested file input
- Why `span` is used for small independently styled text
- Native file selection using `<input type="file">`
- Accepted file extensions using `accept`

## Current Implemented UI

The current frontend contains:

- Full-screen dark gradient background
- Centered glass-style hero card
- Hero label, title, and description
- Styled Create Flashcards button
- `showUpload` Boolean state, initially `false`
- Click handler that changes `showUpload` to `true`
- Conditionally displayed upload area
- Styled clickable `label` around a visually hidden file input
- Accepted formats: PDF, DOC, DOCX, PPT, and PPTX

Relevant files:

- `frontend/src/App.tsx`
- `frontend/src/App.css`
- `frontend/src/index.css`

## Current React Behavior

```text
App starts
  -> showUpload is false
  -> upload area is hidden

User clicks Create flashcards
  -> click handler calls setShowUpload(true)
  -> React renders App again
  -> upload area appears

User clicks upload area
  -> nested file input opens the browser file picker
```

## Next Lesson

Problem: The browser allows a file to be selected, but the custom UI does not remember or display the selected filename.

Next concept: Store the selected `File` object in React state.

Planned state:

```tsx
const [selectedFile, setSelectedFile] = useState<File | null>(null)
```

Before implementing it, explain:

- What the browser `File` type represents
- Why the state can contain either `File` or `null`
- What TypeScript union syntax `File | null` means
- What the input `onChange` event is
- How to safely read the first file from `event.target.files`
- How conditional text can show either the filename or the default instruction

Do not move to backend upload yet. First make file selection and filename display fully understood.

## Small Review Items For Next Session

- The handler is currently named `handelCreateClick`; explain the typo and rename it to `handleCreateClick` when the user is ready.
- `.app` currently has `overflow: hidden`; discuss whether to remove it because it can hide vertically overflowing content on smaller screens.
- Formatting is inconsistent, but defer cleanup until after the current React concept is understood.

## Related Learning Guide

Detailed questions, explanations, Java comparisons, and interview review notes are maintained separately:

- `tasks/react-typescript-learning-guide.md`
