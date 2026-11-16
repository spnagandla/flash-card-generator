import { useRef, useState } from 'react'
import type { ChangeEvent, DragEvent } from 'react'

// Props are inputs that App passes to this child component.
type UploadAreaProps = {
    // App owns this state; UploadArea only reads and displays it.
    selectedFile: File | null
    disabled: boolean
    onRemoveFile: () => void

    // These callbacks let UploadArea report browser events back to App.
    onFileChange: (event: ChangeEvent<HTMLInputElement>) => void
    onDragOver: (event: DragEvent<HTMLDivElement>) => void
    onDrop: (event: DragEvent<HTMLDivElement>) => void
}

// Destructuring extracts each value from the single props object.
function UploadArea({
    selectedFile,
    disabled,
    onRemoveFile,
    onFileChange,
    onDragOver,
    onDrop,
}: Readonly<UploadAreaProps>) {
    const [isDragging, setIsDragging] = useState(false)
    const fileInputRef = useRef<HTMLInputElement>(null)

    function handleDragOver(event: DragEvent<HTMLDivElement>) {
        onDragOver(event)

        if (disabled) {
            return
        }

        setIsDragging(true)
    }

    function handleDragLeave(event: DragEvent<HTMLDivElement>) {
        if (!event.currentTarget.contains(event.relatedTarget as Node | null)) {
            setIsDragging(false)
        }
    }

    function handleDrop(event: DragEvent<HTMLDivElement>) {
        setIsDragging(false)

        if (disabled) {
            event.preventDefault()
            return
        }

        onDrop(event)
    }

    function handleRemoveFile() {
        if (fileInputRef.current !== null) {
            fileInputRef.current.value = ''
        }

        onRemoveFile()
    }

    function formatFileSize(size: number) {
        if (size < 1024 * 1024) {
            return `${Math.max(1, Math.round(size / 1024))} KB`
        }

        return `${(size / (1024 * 1024)).toFixed(1)} MB`
    }

    return (
        <div
            className={`upload-area${isDragging ? ' upload-area-active' : ''}${disabled ? ' upload-area-disabled' : ''}`}
            aria-disabled={disabled}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
        >
            <input
                ref={fileInputRef}
                id="study-material-upload"
                className="file-input"
                type="file"
                accept=".pdf,.doc,.docx,.ppt,.pptx"
                disabled={disabled}
                onChange={onFileChange}
            />

            <label className="upload-main" htmlFor="study-material-upload">
                <span className="upload-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24">
                        <path d="M12 16V4m0 0L7.5 8.5M12 4l4.5 4.5M5 15.5v2A2.5 2.5 0 0 0 7.5 20h9a2.5 2.5 0 0 0 2.5-2.5v-2" />
                    </svg>
                </span>

                <span className="upload-copy">
                    <span className="upload-eyebrow">
                        {isDragging ? 'Release to upload' : 'Add study material'}
                    </span>
                    <span className="upload-title">
                        {isDragging
                            ? 'Drop your document here'
                            : 'Drag and drop your document'}
                    </span>
                    <span className="upload-hint">
                        PDF, Word, or PowerPoint up to 25 MB
                    </span>
                </span>

                <span className="upload-browse">
                    Browse files
                    <svg viewBox="0 0 20 20" aria-hidden="true">
                        <path d="M7.5 5 12.5 10l-5 5" />
                    </svg>
                </span>
            </label>

            {selectedFile && (
                <div className="selected-file">
                    <span className="selected-file-icon" aria-hidden="true">
                        <svg viewBox="0 0 24 24">
                            <path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8l-5-5Z" />
                            <path d="M14 3v5h5M9 14l2 2 4-4" />
                        </svg>
                    </span>
                    <span className="selected-file-copy">
                        <strong>{selectedFile.name}</strong>
                        <span>{formatFileSize(selectedFile.size)} · Ready to generate</span>
                    </span>
                    <button
                        className="selected-file-action"
                        type="button"
                        disabled={disabled}
                        aria-label={`Remove ${selectedFile.name}`}
                        onClick={handleRemoveFile}
                    >
                        &times;
                    </button>
                </div>
            )}

            <span className="upload-footer">
                <span>Private and secure</span>
                <span className="upload-formats" aria-label="Supported file formats">
                    <span>PDF</span>
                    <span>DOCX</span>
                    <span>PPTX</span>
                </span>
            </span>
        </div>
    )
}

export default UploadArea
