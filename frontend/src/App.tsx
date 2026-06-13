import './App.css'
import { useEffect, useState } from 'react'
import type { ChangeEvent, DragEvent } from 'react'
import Toast from './Toast'
import UploadArea from './UploadArea'

const TOAST_DISPLAY_TIME = 4000

type ToastMessage = {
    type: 'error' | 'success'
    title: string
    message: string
}

type UploadResponse = {
    jobId: string
}

function App() {
    const allowedExtensions = ['pdf', 'doc', 'docx', 'ppt', 'pptx']
    const [showUpload, setShowUpload] = useState(false)

    // App owns the selected file because multiple UI states may depend on it later.
    const [selectedFile, setSelectedFile] = useState<File | null>(null)
    const [isUploading, setIsUploading] = useState(false)
    const [jobId, setJobId] = useState<string | null>(null)
    const [toast, setToast] = useState<ToastMessage | null>(null)

    // Start a timer whenever a notification appears.
    useEffect(() => {
        if (toast === null) {
            return
        }

        const timerId = window.setTimeout(() => {
            setToast(null)
        }, TOAST_DISPLAY_TIME)

        return () => window.clearTimeout(timerId)
    }, [toast])

    function isFileAllowed(file: File) {
        const extension = file.name.split('.').pop()?.toLowerCase()
        return extension !== undefined && allowedExtensions.includes(extension)
    }

    function processFile(file: File | null) {
        setJobId(null)

        if (file === null) {
            setSelectedFile(null)
            setToast(null)
            return
        }

        if (!isFileAllowed(file)) {
            setSelectedFile(null)
            setToast({
                type: 'error',
                title: 'Unsupported file',
                message: 'Please select a PDF, Word, or PowerPoint file.',
            })
            return
        }

        setSelectedFile(file)
        setToast({
            type: 'success',
            title: 'File added',
            message: `${file.name} is ready to generate.`,
        })
    }

    function handleCreateClick() {
        setShowUpload(true)
    }

    function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
        const file = event.target.files?.[0] ?? null
        processFile(file)
    }

    function handleDragOver(event: DragEvent<HTMLLabelElement>) {
        event.preventDefault()
    }

    function handleDrop(event: DragEvent<HTMLLabelElement>) {
        event.preventDefault()

        const file = event.dataTransfer.files[0]
        processFile(file)
    }

    async function handleGenerateClick() {
        if (selectedFile === null || isUploading) {
            return
        }

        const formData = new FormData()
        formData.append('file', selectedFile)

        setIsUploading(true)
        setJobId(null)

        try {
            const response = await fetch('/api/flashcards/upload', {
                method: 'POST',
                body: formData,
            })

            if (!response.ok) {
                throw new Error(`Upload failed with status ${response.status}`)
            }

            const uploadResponse: UploadResponse = await response.json()
            setJobId(uploadResponse.jobId)
            setToast({
                type: 'success',
                title: 'Generation started',
                message: `${selectedFile.name} was sent for processing.`,
            })
        } catch (error) {
            console.error('Failed to upload document:', error)
            setToast({
                type: 'error',
                title: 'Upload failed',
                message: 'Could not reach the server. Please try again.',
            })
        } finally {
            setIsUploading(false)
        }
    }

    return (
        <main className="app">
            <section className="hero">
                <p className="hero-label">AI-powered study companion</p>
                <h1 className="hero-title">
                    Turn your documents into smarter flashcards
                </h1>
                <p className="hero-description">
                    Upload your study material and generate an interactive flashcard
                    deck in minutes.
                </p>

                <button
                    className="primary-button"
                    type="button"
                    onClick={handleCreateClick}
                >
                    Create flashcards
                </button>
                {showUpload && (
                    <>
                        {/* State and callback functions pass from App to UploadArea as props. */}
                        <UploadArea
                            selectedFile={selectedFile}
                            onFileChange={handleFileChange}
                            onDragOver={handleDragOver}
                            onDrop={handleDrop}
                        />

                        {selectedFile && (
                            <div className="generate-actions">
                                <button
                                    className="generate-button"
                                    type="button"
                                    disabled={isUploading}
                                    onClick={handleGenerateClick}
                                >
                                    {isUploading && (
                                        <span className="button-spinner" aria-hidden="true" />
                                    )}
                                    {isUploading
                                        ? 'Sending document...'
                                        : 'Generate flashcards'}
                                </button>

                                {jobId && (
                                    <p className="job-reference">
                                        Job started: <code>{jobId}</code>
                                    </p>
                                )}
                            </div>
                        )}
                    </>
                )}
            </section>
            {toast && (
                <Toast
                    type={toast.type}
                    title={toast.title}
                    message={toast.message}
                />
            )}
        </main>
    )
}

export default App
