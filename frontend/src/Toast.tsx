type ToastProps = {
    type: 'error' | 'success'
    title: string
    message: string
    onClose: () => void
}

function Toast({
    type,
    title,
    message,
    onClose,
}: Readonly<ToastProps>) {
    return (
        <aside
            className={`toast toast-${type}`}
            role={type === 'error' ? 'alert' : 'status'}
        >
            <div className="toast-content">
                <span className="toast-icon" aria-hidden="true">
                    {type === 'success' ? '✓' : '!'}
                </span>
                <div className="toast-copy">
                    <strong>{title}</strong>
                    <p>{message}</p>
                </div>
                <button
                    className="toast-close"
                    type="button"
                    aria-label="Close notification"
                    onClick={onClose}
                >
                    &times;
                </button>
            </div>
            <div className="toast-progress" />
        </aside>
    )
}

export default Toast
