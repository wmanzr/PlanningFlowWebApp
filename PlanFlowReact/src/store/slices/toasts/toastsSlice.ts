import { createSlice, nanoid, type PayloadAction } from '@reduxjs/toolkit';
export type ToastLevel = 'info' | 'success' | 'warning' | 'error';
export interface Toast {
    id: string;
    level: ToastLevel;
    message: string;
    href: string | null;
    ttlMs: number;
    createdAt: number;
    closing: boolean;
}
interface ToastsState {
    queue: Toast[];
}
const initialState: ToastsState = { queue: [] };
const DEFAULT_TTL_MS = 5000;
interface PushPayload {
    level: ToastLevel;
    message: string;
    href?: string;
    ttlMs?: number;
}
export const toastsSlice = createSlice({
    name: 'toasts',
    initialState,
    reducers: {
        push: {
            reducer: (state, action: PayloadAction<Toast>) => {
                state.queue.push(action.payload);
            },
            prepare: (payload: PushPayload) => ({
                payload: {
                    id: nanoid(),
                    level: payload.level,
                    message: payload.message,
                    href: payload.href ?? null,
                    ttlMs: payload.ttlMs ?? DEFAULT_TTL_MS,
                    createdAt: Date.now(),
                    closing: false,
                },
            }),
        },
        beginDismiss(state, action: PayloadAction<string>) {
            const toast = state.queue.find((t) => t.id === action.payload);
            if (toast)
                toast.closing = true;
        },
        dismiss(state, action: PayloadAction<string>) {
            state.queue = state.queue.filter((toast) => toast.id !== action.payload);
        },
        clear(state) {
            state.queue = [];
        },
    },
});
export const toastsActions = toastsSlice.actions;
export const toastsReducer = toastsSlice.reducer;
