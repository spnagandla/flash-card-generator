import './App.css'
import { useEffect, useState } from 'react'
import type { ChangeEvent, DragEvent } from 'react'
import Toast from './Toast'
import UploadArea from './UploadArea'

const TOAST_DISPLAY_TIME = 4000
const STATUS_RETRY_DELAY = 10_000

type ToastMessage = {
    type: 'error' | 'success'
    title: string
    message: string
}

type UploadResponse = {
    jobId: string
}

type JobStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'

type JobStatusResponse = {
    jobId: string
    status: JobStatus
    fileName: string
    progress: number
    error: string | null
}

function App() {
    const allowedExtensions = ['pdf', 'doc', 'docx', 'ppt', 'pptx']
    const [showUpload, setShowUpload] = useState(false)

    // App owns the selected file because multiple UI states may depend on it later.
    const [selectedFile, setSelectedFile] = useState<File | null>(null)
    const [isUploading, setIsUploading] = useState(false)
    const [jobId, setJobId] = useState<string | null>(null)
    const [jobStatus, setJobStatus] = useState<JobStatusResponse | null>(null)
    const [toast, setToast] = useState<ToastMessage | null>(null)

    const isJobProcessing =
        jobId !== null &&
        (jobStatus === null ||
            jobStatus.status === 'PENDING' ||
            jobStatus.status === 'PROCESSING')
    const isBusy = isUploading || isJobProcessing

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

    // A jobId means the backend accepted the file. Long-poll until that job finishes.
    useEffect(() => {
        if (jobId === null) {
            return
        }

        const abortController = new AbortController()
        let timeoutId: number | undefined
        let hasShownStatusError = false
        let lastKnownStatus: JobStatus | null = null

        async function checkJobStatus() {
            try {
                const statusQuery = lastKnownStatus === null
                    ? ''
                    : `?knownStatus=${encodeURIComponent(lastKnownStatus)}`
                const response = await fetch(
                    `/api/flashcards/status/${jobId}/wait${statusQuery}`,
                    { signal: abortController.signal },
                )

                if (response.status === 204) {
                    checkJobStatus()
                    return
                }

                if (!response.ok) {
                    throw new Error(`Status request failed with status ${response.status}`)
                }

                const statusResponse: JobStatusResponse = await response.json()
                setJobStatus(statusResponse)
                lastKnownStatus = statusResponse.status
                hasShownStatusError = false

                const isFinished =
                    statusResponse.status === 'COMPLETED' ||
                    statusResponse.status === 'FAILED'

                if (!isFinished) {
                    checkJobStatus()
                }
            } catch (error) {
                if (error instanceof DOMException && error.name === 'AbortError') {
                    return
                }

                console.error('Failed to check job status:', error)

                if (!hasShownStatusError) {
                    setToast({
                        type: 'error',
                        title: 'Status check failed',
                        message: 'Retrying the generation status automatically.',
                    })
                    hasShownStatusError = true
                }

                timeoutId = window.setTimeout(checkJobStatus, STATUS_RETRY_DELAY)
            }
        }

        checkJobStatus()

        return () => {
            abortController.abort()

            if (timeoutId !== undefined) {
                window.clearTimeout(timeoutId)
            }
        }
    }, [jobId])

    function isFileAllowed(file: File) {
        const extension = file.name.split('.').pop()?.toLowerCase()
        return extension !== undefined && allowedExtensions.includes(extension)
    }

    function processFile(file: File | null) {
        setJobId(null)
        setJobStatus(null)

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
        if (selectedFile === null || isBusy) {
            return
        }

        const formData = new FormData()
        formData.append('file', selectedFile)

        setIsUploading(true)
        setJobId(null)
        setJobStatus(null)

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
                            disabled={isBusy}
                            onFileChange={handleFileChange}
                            onDragOver={handleDragOver}
                            onDrop={handleDrop}
                        />

                        {selectedFile && (
                            <div className="generate-actions">
                                <button
                                    className="generate-button"
                                    type="button"
                                    disabled={isBusy}
                                    onClick={handleGenerateClick}
                                >
                                    {isBusy && (
                                        <span className="button-spinner" aria-hidden="true" />
                                    )}
                                    {isUploading
                                        ? 'Uploading document...'
                                        : isJobProcessing
                                          ? 'Generating flashcards...'
                                          : 'Generate flashcards'}
                                </button>

                                {jobId && jobStatus === null && (
                                    <p className="job-reference" role="status">
                                        Waiting for processing to start...
                                    </p>
                                )}

                                {jobStatus && (
                                    <div className="job-status" role="status">
                                        <div className="job-status-heading">
                                            <strong>{jobStatus.fileName}</strong>
                                            <span>{jobStatus.status}</span>
                                        </div>

                                        {(jobStatus.status === 'PENDING' ||
                                            jobStatus.status === 'PROCESSING') && (
                                            <>
                                                <progress
                                                    aria-label="Flashcard generation in progress"
                                                />
                                                <p>
                                                    {jobStatus.status === 'PENDING'
                                                        ? 'Waiting for processing to start...'
                                                        : 'Generating your flashcards...'}
                                                </p>
                                            </>
                                        )}

                                        {jobStatus.status === 'COMPLETED' && (
                                            <p>Your flashcards are ready.</p>
                                        )}

                                        {jobStatus.status === 'FAILED' && (
                                            <p>
                                                Generation failed:{' '}
                                                {jobStatus.error ?? 'Unknown error'}
                                            </p>
                                        )}
                                    </div>
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
