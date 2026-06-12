# React And TypeScript Learning Guide

Use this file as:

- a beginner-friendly study guide,
- a quick interview revision sheet,
- a record of questions asked while building the UI,
- and supporting context for future learning sessions.

This is not the progress tracker. Current implementation status and the next coding step are in:

- `tasks/react-ui-progress-2026-06-12.md`

## 1. Did We Use The Factory Design Pattern?

### Question

Did we use the Factory design pattern in this project?

### Answer

Not as a classic Factory Pattern. The document parsing code mainly uses the Strategy Pattern:

- `DocumentParser` defines the common strategy interface.
- `PdfParser`, `PptParser`, and `WordParser` are concrete strategies.
- `ParserService` selects a parser by calling `supports(extension)`.
- Spring creates and injects parser objects as `List<DocumentParser>`.

`ParserService` behaves somewhat like a resolver, but it does not construct parser objects itself.

### Interview Answer

> The parser implementation uses the Strategy Pattern with Spring dependency injection. Each document parser implements the same interface, and `ParserService` selects the strategy that supports the file extension. It is not a classic factory because the service does not instantiate the parser objects.

## 2. How The Frontend Project Is Organized

### Question

What is the structure of the frontend folder?

### Important Files

- `src/`: application source code
- `src/main.tsx`: frontend entry point that renders `<App />`
- `src/App.tsx`: main React component
- `src/App.css`: styles associated with the app UI
- `src/index.css`: global styles
- `src/assets/`: images imported by components
- `public/`: static files served directly
- `index.html`: contains `<div id="root"></div>`
- `package.json`: dependencies and npm scripts
- `node_modules/`: installed packages; do not edit manually
- `vite.config.ts`: Vite configuration
- `tsconfig*.json`: TypeScript configuration
- `eslint.config.js`: code-quality rules

### Application Flow

```text
index.html
    -> main.tsx
    -> <App />
    -> App.tsx
    -> child components
```

### Interview Answer

> Vite serves `index.html`, which contains the root DOM element. `main.tsx` is the JavaScript entry point and mounts the React application into that root. It renders the top-level `App` component, which then composes the rest of the component tree.

## 3. Why Replace The Vite Starter UI?

### Question

Why did we remove the content Vite created in `App.tsx`?

### Answer

The original page is sample content containing logos, a counter, and documentation links. It demonstrates that React and Vite work, but it is not required by the application.

We retain the actual project setup:

- `main.tsx` starts React.
- `index.html` supplies the root element.
- Vite runs the development server.
- React and TypeScript remain configured.
- Dependencies remain installed.

Only the sample UI is replaced with our flashcard UI.

## 4. What Does `function App()` Mean?

```tsx
function App() {
  return <h1>Hello</h1>
}
```

### Syntax

- `function`: declares a JavaScript function.
- `App`: the function name.
- `()`: contains parameters.
- Empty `()` means we are not declaring any parameters.
- `{}`: contains the function body.
- `return`: returns the JSX that describes the UI.

It becomes a React function component because React renders it as:

```tsx
<App />
```

### Why Is `App` Capitalized?

React treats lowercase JSX names as browser elements:

```tsx
<main />
<button />
```

React treats capitalized names as custom components:

```tsx
<App />
<Flashcard />
```

### Why Does It Have No Parameters?

Components normally receive one input object called `props`. `App` currently receives no data from its parent, so no parameter is needed.

Example with props:

```tsx
type GreetingProps = {
  name: string
}

function Greeting({ name }: GreetingProps) {
  return <h1>Hello, {name}</h1>
}
```

Usage:

```tsx
<Greeting name="Pavan" />
```

### Java Comparison

```java
public String greeting(String name) {
    return "Hello, " + name;
}
```

The React component returns a UI description instead of a plain string.

### Interview Answer

> A React function component is a JavaScript or TypeScript function that returns JSX. React calls it as part of rendering the component tree. Component names are capitalized so React can distinguish them from native HTML elements.

## 5. Is It HTML, JSX, Or TSX?

### Question

It looks like HTML. Why is it called JSX?

### Answer

The elements are based on HTML, but when HTML-like syntax is written inside JavaScript, it is JSX. When it appears inside a TypeScript `.tsx` file, it is commonly called TSX.

```tsx
<h1 className="title">Hello</h1>
```

React transforms this into JavaScript similar to:

```tsx
React.createElement("h1", { className: "title" }, "Hello")
```

JSX also allows JavaScript expressions inside braces:

```tsx
const username = 'Pavan'

return <h1>Hello, {username}</h1>
```

### Interview Answer

> JSX is a syntax extension that lets developers describe UI using HTML-like syntax inside JavaScript. TSX provides the same capability in TypeScript files. JSX is compiled into JavaScript calls that create React elements.

## 6. What Is A React Component?

### Question

Does one component mean one box?

### Answer

Sometimes a component appears as one visual box, but that is not its definition.

A component is an independent, potentially reusable piece of UI and behavior. It can represent:

- a button,
- an upload area,
- a flashcard,
- a header,
- or an entire page.

Create a component when a UI part:

- has a clear responsibility,
- is reused,
- has its own behavior,
- or is large enough to organize separately.

Do not create a component for every HTML element.

### Interview Answer

> A React component is a reusable unit that encapsulates UI structure and optionally behavior. Components compose other components to form the application tree.

## 7. What Are Semantic HTML Elements?

### Question

What is `<section>`?

### Answer

`<section>` groups related content around one meaningful topic.

```tsx
<section>
  <h1>Flashcard Generator</h1>
  <p>Convert documents into flashcards.</p>
</section>
```

It does not add visual styling by itself.

Related elements:

- `<main>`: primary page content
- `<section>`: meaningful content group
- `<header>`: introductory content or navigation
- `<footer>`: bottom information
- `<div>`: generic container without semantic meaning
- `<span>`: generic inline container, usually for small text
- `<label>`: describes or activates a form input

### Interview Answer

> Semantic HTML communicates the meaning and structure of content to browsers, accessibility tools, search engines, and developers. A `section` represents a thematic group, while a `div` is a generic container.

## 8. How JSX Connects To CSS

### Question

What does connecting JSX to CSS mean?

### Answer

Give the JSX element a class:

```tsx
<h1 className="hero-title">Flashcard Generator</h1>
```

Target the same class from CSS:

```css
.hero-title {
  color: white;
}
```

The dot in `.hero-title` means “select elements with this class.”

The CSS file must be imported:

```tsx
import './App.css'
```

### Why Add Different Classes?

Different elements need different styling:

```tsx
<p className="hero-label">AI-powered study companion</p>
<h1 className="hero-title">Turn documents into flashcards</h1>
<p className="hero-description">Upload your material...</p>
<button className="primary-button">Create flashcards</button>
```

A class is not required on every element. Add one when the element needs specific styling or identification.

### Interview Answer

> React uses `className` to assign CSS classes in JSX. Stylesheets can then target those classes with standard CSS selectors. Classes should represent meaningful UI roles rather than being added without purpose.

## 9. Full Browser Width And CSS Sizing

### Question

Why was there black space on the left and right?

### Answer

The Vite starter CSS limited the root element:

```css
#root {
  width: 1126px;
}
```

It was replaced with:

```css
#root {
  width: 100%;
  min-height: 100vh;
}
```

The app container also uses:

```css
.app {
  box-sizing: border-box;
  width: 100%;
  min-height: 100vh;
}
```

`box-sizing: border-box` includes padding and borders inside the declared width.

### `100vh`

`100vh` means 100 percent of the browser viewport height.

### Interview Answer

> The root container had a fixed maximum width from the starter template. Setting the root and application shell to `width: 100%` allows the UI to occupy the viewport, while `box-sizing: border-box` prevents padding from increasing the computed width.

## 10. What Does `overflow: hidden` Do?

### Answer

It clips child content that extends beyond the parent boundary:

```css
.app {
  overflow: hidden;
}
```

This can prevent decorative shapes from causing scrollbars, but it can also hide real content. It is not currently necessary for this app.

For horizontal overflow only:

```css
overflow-x: hidden;
```

### Interview Answer

> The CSS `overflow` property controls how content outside an element's bounds is handled. `hidden` clips it, while `auto` adds scrolling when needed. It should be used carefully because it can make content inaccessible.

## 11. Modern CSS Concepts Used

### Responsive Width

```css
width: min(900px, 100%);
```

The element uses 900px when space is available, but never exceeds the available width.

### Responsive Font Size

```css
font-size: clamp(42px, 6vw, 72px);
```

- minimum: `42px`
- preferred: `6vw`
- maximum: `72px`

### Transparent Color

```css
background: rgba(255, 255, 255, 0.08);
```

The fourth value controls opacity.

### Glass Effect

```css
backdrop-filter: blur(18px);
```

This blurs content behind the element.

### Interaction States

```css
.primary-button:hover {
  transform: translateY(-3px);
}

.primary-button:active {
  transform: translateY(0);
}
```

- `:hover`: pointer is over the element
- `:active`: element is being pressed

### Smooth Change

```css
transition: transform 180ms ease;
```

This animates changes to `transform` over 180 milliseconds.

## 12. React Events

### Problem

The Create Flashcards button looked clickable but did not perform an action.

### Solution

Define an event handler:

```tsx
function handleCreateClick() {
  alert('File upload is coming next!')
}
```

Give the function to the button:

```tsx
<button onClick={handleCreateClick}>
  Create flashcards
</button>
```

### Important Difference

Correct:

```tsx
onClick={handleCreateClick}
```

This passes the function so React can call it later.

Incorrect for this use:

```tsx
onClick={handleCreateClick()}
```

This executes the function immediately during rendering.

### Java Comparison

The handler is similar to a callback method registered with an event listener.

### Interview Answer

> React events use camelCase props such as `onClick`. A handler function is passed as a reference, and React invokes it when the event occurs.

## 13. What Is React State?

### Problem

The upload area should be:

```text
hidden before the click
visible after the click
```

React needs to remember which situation the UI is currently in.

### Definition

State is component-owned data that can change while the application runs. Updating state tells React to render the component again using the new value.

```tsx
const [showUpload, setShowUpload] = useState(false)
```

- `showUpload`: current state value
- `setShowUpload`: function used to request a state update
- `false`: initial value

### Why Not A Normal Variable?

This is insufficient:

```tsx
let showUpload = false

function handleCreateClick() {
  showUpload = true
}
```

Changing the variable does not tell React to update the screen.

State solves both problems:

1. React remembers the value between renders.
2. React renders the component again after the update.

### Why `const` If State Changes?

We never assign directly:

```tsx
showUpload = true // wrong
```

We call the setter:

```tsx
setShowUpload(true)
```

React stores the new state and runs the component again with the latest value.

### Flow

```text
User clicks
    -> setShowUpload(true)
    -> React stores true
    -> React renders App again
    -> upload area appears
```

### Interview Answer

> State is data managed by a component that affects rendered output. Calling its setter schedules a re-render. State should not be mutated directly because React relies on its update mechanism to track changes.

## 14. What Is `useState`?

`useState` is a React Hook that gives a function component state.

Import it at the top of the file:

```tsx
import { useState } from 'react'
```

Use it inside the component:

```tsx
function App() {
  const [showUpload, setShowUpload] = useState(false)
}
```

Conceptually, `useState` returns two values:

```text
current state value
function that updates the state
```

The syntax:

```tsx
const [showUpload, setShowUpload] = ...
```

is array destructuring.

Equivalent longer form:

```tsx
const stateResult = useState(false)
const showUpload = stateResult[0]
const setShowUpload = stateResult[1]
```

### Interview Answer

> `useState` is a Hook for adding local state to a function component. It returns the current state and a setter function. Calling the setter schedules a render with the updated value.

## 15. Conditional Rendering

### Problem

The upload area must only appear when `showUpload` is true.

### Code

```tsx
{showUpload && (
  <div className="upload-area">
    <h2>Upload your document</h2>
    <p>PDF, Word, or PowerPoint</p>
  </div>
)}
```

### Syntax

- `{}`: execute a JavaScript expression inside JSX.
- `showUpload`: Boolean condition.
- `&&`: render the right side only when the left side is truthy.
- `()`: group multiline JSX.

Java-style idea:

```java
if (showUpload) {
    displayUploadArea();
}
```

### Interview Answer

> Conditional rendering means choosing UI based on application data. React commonly uses JavaScript operators such as `&&` for optional content and ternaries when both true and false outputs are needed.

## 16. The Standard File Input

### Problem

The upload area looked interactive, but users could not select a document.

### Solution

```tsx
<input
  type="file"
  accept=".pdf,.doc,.docx,.ppt,.pptx"
/>
```

- `type="file"` creates the browser file picker.
- `accept` filters the file types offered by the picker.

`accept` is not security validation. The backend must still validate file type, content, and size.

### Interview Answer

> A native file input is the standard accessible foundation for file selection. The `accept` attribute improves user experience but must not replace server-side validation.

## 17. Why Use A `label` For The Upload Area?

### Problem

The native file input works but does not match the custom design.

### Solution

Place the input inside a styled label:

```tsx
<label className="upload-area">
  <input className="file-input" type="file" />
  <span className="upload-title">Choose a file</span>
</label>
```

Clicking the label activates its nested input:

```text
Click label
    -> browser activates nested input
    -> file picker opens
```

A `div` does not provide that built-in form behavior.

### What Is `span`?

`span` is a generic inline container used for small pieces of text:

```tsx
<span className="upload-title">Choose a file</span>
<span className="upload-formats">PDF, Word, or PowerPoint</span>
```

It allows the two text roles to be styled independently.

### Structure

```text
label.upload-area
├── input.file-input
├── span.upload-title
└── span.upload-formats
```

### Interview Answer

> Wrapping a file input in a label makes the whole custom upload surface activate the native control. This preserves native browser behavior while allowing a custom visual design.

## 18. Visually Hiding The File Input

```css
.file-input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
}
```

The input remains in the document and continues providing native file selection, while the label becomes the visible control.

The goal is to hide it visually, not remove it from the UI logic.

Accessibility must be verified as the component evolves, especially focus styles and keyboard behavior.

## 19. Is This A Modern Company Approach?

Yes. Native browser controls remain the functional foundation, while companies build a branded UI around them.

A production-quality uploader commonly includes:

- click to select,
- drag and drop,
- allowed file types,
- maximum file size,
- selected filename,
- replace and remove actions,
- upload progress,
- clear error messages,
- keyboard accessibility,
- server-side validation.

Our implementation is intentionally being built in small educational steps.

## 20. Running The Frontend

```powershell
cd C:\Users\pavan\Downloads\flash-card-generator\frontend
npm run dev
```

Vite normally prints a URL such as:

```text
http://localhost:5173/
```

Keep the terminal running. Saving source files triggers development updates. Stop the server with `Ctrl+C`.

## 21. Current Next Concept: Selected File State

### Problem

The browser lets the user select a file, but the custom UI does not remember or display the filename.

### Planned State

```tsx
const [selectedFile, setSelectedFile] = useState<File | null>(null)
```

This must be understood before implementing it:

- `File` is a browser object representing a selected file.
- `null` means no file has been selected.
- `File | null` is a TypeScript union type.
- `onChange` runs when the input selection changes.
- `event.target.files` contains the selected files.
- The UI can use a ternary to show either the filename or default text.

Do not jump directly to backend upload. First understand file selection, events, state, and TypeScript types.

## 22. Interview Quick Review

### What is React?

React is a library for building component-based user interfaces. It renders UI from data and updates the relevant DOM when state or props change.

### What is JSX?

JSX is HTML-like syntax used inside JavaScript to describe React elements.

### What is TSX?

TSX is JSX syntax inside a TypeScript file.

### What is a component?

A component is a reusable unit of UI and behavior.

### What are props?

Props are read-only inputs passed from a parent component to a child component.

### What is state?

State is component-managed data that can change and trigger rendering.

### What is a Hook?

A Hook is a React function that gives function components features such as state or lifecycle behavior.

### What does `useState` return?

It returns the current state value and a setter function.

### Why should state not be changed directly?

Direct changes bypass React's state update mechanism and may not trigger a render.

### What is conditional rendering?

Conditional rendering displays different UI based on application data.

### Why use semantic HTML?

It improves structure, accessibility, maintainability, and search-engine understanding.

### Why use the native file input?

It provides standard browser file selection and accessibility behavior. Custom styling can be built around it.
