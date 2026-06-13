type ToastProps = {
    type: 'error' | 'success'
    title: string
    message: string
}

function Toast({
    type,
    title,
    message,
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
                <div>
                    <strong>{title}</strong>
                    <p>{message}</p>
                </div>
            </div>
            <div className="toast-progress" />
        </aside>
    )
}

export default Toast
