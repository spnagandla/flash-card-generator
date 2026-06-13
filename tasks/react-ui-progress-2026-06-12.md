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
- Selected-file state using `File | null`
- Input change events and reading `event.target.files`
- Conditional filename display
- Drag events using `onDragOver` and `onDrop`
- Reading dropped files from `event.dataTransfer.files`
- Using `preventDefault()` to allow a browser drop operation

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
- Selected files are stored in React state
- The selected filename is displayed in the upload area
- Files can be selected with the browser picker or dragged onto the upload area

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

User selects a file
  -> input onChange handler reads the first file
  -> selectedFile state is updated
  -> React renders the selected filename

User drags a file onto the upload area
  -> drag-over default behavior is prevented
  -> drop handler reads the first transferred file
  -> selectedFile state is updated
  -> React renders the selected filename
```

## Next Lesson

Problem: The upload markup and its event wiring currently live directly inside `App`. The next goal is to understand how React components communicate before deciding whether to extract the upload area.

Next concept: Props and reusable components.

Before showing extraction code, explain:

- Why component extraction may improve organization
- Why extraction is optional at the current size
- The difference between state and props
- Why `App` should continue owning `selectedFile`
- How a parent passes values and event-handler functions to a child
- What a TypeScript props type represents
- What function types such as `(event: SomeEvent) => void` mean
- What object destructuring in a component parameter means

Do not present the complete component extraction as an unexplained code block. Teach the data flow first, then implement it in one understandable step.

## Small Review Items For Next Session

- The handlers are currently named `handelCreateClick` and `handelDrop`; explain the typo and rename them to `handleCreateClick` and `handleDrop` when the user is ready.
- `.app` currently has `overflow: hidden`; discuss whether to remove it because it can hide vertically overflowing content on smaller screens.
- Formatting is inconsistent, but defer cleanup until after the current React concept is understood.

## Related Learning Guide

Detailed questions, explanations, Java comparisons, and interview review notes are maintained separately:

- `tasks/react-typescript-learning-guide.md`
