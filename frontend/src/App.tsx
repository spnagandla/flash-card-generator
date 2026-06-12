import './App.css'
import {useState} from 'react'

function App() {
    const [showUpload, setShowUpload] = useState(false);

    function handelCreateClick() {
        setShowUpload(true)
    }

    return (
        <main className="app">
            <section className="hero">
                <p className="hero-label">AI-powered study companion</p>
                <h1 className="hero-title">Turn your documents into smarter flashcards</h1>
                <p className="hero-description">
                    Upload your study material and generate an interactive flashcard
                    deck in minutes.
                </p>

                <button className="primary-button" type="button" onClick={handelCreateClick}>Create flashcards</button>
                {showUpload && (
                    <label className="upload-area">
                        <input
                            className="file-input"
                            type="file"
                            accept=".pdf,.doc,.docx,.ppt,.pptx"
                        />

                        <span className="upload-title">Choose a file or drag it here</span>
                        <span className="upload-formats">
        PDF, Word, or PowerPoint
      </span>
                    </label>
                )}
            </section>
        </main>
    )
}

export default App