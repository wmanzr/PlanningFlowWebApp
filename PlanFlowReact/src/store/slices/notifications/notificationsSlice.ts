import { createAsyncThunk, createSlice, nanoid, type PayloadAction } from '@reduxjs/toolkit';
import { notificationsApi, parseApiError, type ListNotificationsQuery } from '@/api';
import type { AppApiError, AsyncStatus } from '@/types';
export interface AppNotification {
    id: string;
    createdAt: number;
    title: string | null;
    message: string;
    readAt: number | null;
}
interface NotificationsState {
    items: AppNotification[];
    unreadCount: number;
    listStatus: AsyncStatus;
    listError: AppApiError | null;
    totalPages: number;
    totalElements: number;
}
const initialState: NotificationsState = {
    items: [],
    unreadCount: 0,
    listStatus: 'idle',
    listError: null,
    totalPages: 1,
    totalElements: 0,
};
type IncomingNotification = {
    id?: string;
    createdAt?: number;
    title?: string;
    message: string;
} | {
    title?: string;
    message: string;
};
export const notificationsSlice = createSlice({
    name: 'notifications',
    initialState,
    reducers: {
        received(state, action: PayloadAction<IncomingNotification>) {
            const now = Date.now();
            const payload = action.payload;
            const item: AppNotification = {
                id: 'id' in payload && payload.id ? payload.id : nanoid(),
                createdAt: 'createdAt' in payload && typeof payload.createdAt === 'number' ? payload.createdAt : now,
                title: payload.title ?? null,
                message: payload.message,
                readAt: null,
            };
            state.items.unshift(item);
            state.unreadCount += 1;
        },
        markRead(state, action: PayloadAction<string>) {
            const item = state.items.find((n) => n.id === action.payload);
            if (item && item.readAt === null) {
                item.readAt = Date.now();
                state.unreadCount = Math.max(0, state.unreadCount - 1);
            }
        },
        markAllRead(state) {
            const now = Date.now();
            state.items.forEach((n) => {
                if (n.readAt === null)
                    n.readAt = now;
            });
            state.unreadCount = 0;
        },
        clear(state) {
            state.items = [];
            state.unreadCount = 0;
            state.listStatus = 'idle';
            state.listError = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchNotificationsThunk.pending, (state) => {
            state.listStatus = 'pending';
            state.listError = null;
        })
            .addCase(fetchNotificationsThunk.fulfilled, (state, action) => {
            state.listStatus = 'succeeded';
            state.listError = null;
            state.items = action.payload.items;
            state.unreadCount = action.payload.unreadCount;
            state.totalPages = action.payload.totalPages;
            state.totalElements = action.payload.totalElements;
        })
            .addCase(fetchNotificationsThunk.rejected, (state, action) => {
            state.listStatus = 'failed';
            state.listError = (action.payload as AppApiError | undefined) ?? parseApiError(action.error);
        })
            .addCase(fetchUnreadCountThunk.fulfilled, (state, action) => {
            state.unreadCount = action.payload;
        });
    },
});
export const notificationsActions = notificationsSlice.actions;
export const notificationsReducer = notificationsSlice.reducer;
export const fetchUnreadCountThunk = createAsyncThunk<number, void, {
    rejectValue: AppApiError;
}>('notifications/unreadCount', async (_, { rejectWithValue }) => {
    try {
        return await notificationsApi.unreadCount();
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const fetchNotificationsThunk = createAsyncThunk<{
    items: AppNotification[];
    unreadCount: number;
    totalPages: number;
    totalElements: number;
}, ListNotificationsQuery, {
    rejectValue: AppApiError;
}>('notifications/list', async (query, { rejectWithValue }) => {
    try {
        const page = await notificationsApi.list(query);
        const items: AppNotification[] = page.items.map((n) => ({
            id: String(n.id),
            createdAt: new Date(n.createdAt).getTime(),
            title: n.title ?? null,
            message: n.message,
            readAt: n.readAt ? new Date(n.readAt).getTime() : null,
        }));
        const unreadCount = items.reduce((acc, n) => (n.readAt === null ? acc + 1 : acc), 0);
        return { items, unreadCount, totalPages: page.totalPages, totalElements: page.totalElements };
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const markNotificationReadThunk = createAsyncThunk<string, {
    id: string;
}, {
    rejectValue: AppApiError;
}>('notifications/markRead', async ({ id }, { rejectWithValue }) => {
    try {
        await notificationsApi.markRead(Number(id));
        return id;
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const markAllNotificationsReadThunk = createAsyncThunk<void, void, {
    rejectValue: AppApiError;
}>('notifications/markAllRead', async (_, { rejectWithValue }) => {
    try {
        await notificationsApi.markAllRead();
        return;
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
