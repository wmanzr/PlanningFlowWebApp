import { useEffect, useRef } from 'react';
import { useAppDispatch } from '@/store';
import { toastsActions } from '@/store/slices/toasts/toastsSlice';
export interface ErrorMessageProps {
    title?: string;
    message: string;
    onShown?: () => void;
}
const recentErrorToastAt = new Map<string, number>();
const ERROR_TOAST_DEDUP_MS = 4000;
export const ErrorMessage = ({ title, message, onShown }: ErrorMessageProps) => {
    const dispatch = useAppDispatch();
    const pushedRef = useRef<string | null>(null);
    const text = title ? `${title}: ${message}` : message;
    useEffect(() => {
        if (!text)
            return;
        if (pushedRef.current === text)
            return;
        pushedRef.current = text;
        const now = Date.now();
        const last = recentErrorToastAt.get(text) ?? 0;
        if (now - last < ERROR_TOAST_DEDUP_MS) {
            return;
        }
        recentErrorToastAt.set(text, now);
        dispatch(toastsActions.push({ level: 'error', message: text, ttlMs: 5000 }));
        onShown?.();
    }, [dispatch, onShown, text]);
    return null;
};
