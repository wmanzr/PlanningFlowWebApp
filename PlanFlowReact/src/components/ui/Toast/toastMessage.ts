export type ToastLevelForMessage = 'info' | 'success' | 'warning' | 'error';
export function toastDisplayMessage(level: ToastLevelForMessage, message: string): string {
    if (level !== 'error') {
        return message;
    }
    const t = message.trim().replace(/^!+\s*/, '');
    return t;
}
