import { isAnyOf, isRejected } from '@reduxjs/toolkit';
import { eventsSocket, isExternalSupplierError, isNetworkError } from '@/api';
import { PATHS } from '@/pages/paths';
import { startAppListening } from './middleware';
import { authActions, loginThunk, restoreSessionThunk } from './slices/auth/authSlice';
import { fetchNotificationsThunk, fetchUnreadCountThunk, markAllNotificationsReadThunk, markNotificationReadThunk, notificationsActions, } from './slices/notifications/notificationsSlice';
import { toastsActions } from './slices/toasts/toastsSlice';
import { fetchEventByIdThunk } from './slices/events/eventsSlice';
import { fetchTaskByIdThunk } from './slices/tasks/tasksSlice';
import type { AppApiError } from '@/types';
const WS_TYPES_WITH_USER_MESSAGE = new Set([
    'NOTIFICATION',
    'NOTIFICATION_CREATED',
    'ASSIGNMENT_ASSIGNED',
    'assignment.assigned',
    'ASSIGNMENT_REMOVED',
    'assignment.removed',
    'assignment.accepted',
    'assignment.rejected',
    'incident.reported',
    'coordinator.assigned',
]);
function hrefForWsNotification(type: string, payload: Record<string, unknown>): string {
    if ((type === 'assignment.removed' || type === 'ASSIGNMENT_REMOVED') &&
        typeof payload.eventId === 'number' &&
        typeof payload.taskId === 'number') {
        return PATHS.taskDetail(payload.eventId, payload.taskId);
    }
    if (type === 'coordinator.assigned' && typeof payload.eventId === 'number') {
        return PATHS.eventDetail(payload.eventId);
    }
    if (type === 'incident.reported' && typeof payload.incidentId === 'number') {
        return PATHS.incidentDetail(payload.incidentId);
    }
    const nid = typeof payload.notificationId === 'number' ? payload.notificationId : null;
    if (nid !== null) {
        return `${PATHS.notifications}?focus=${encodeURIComponent(String(nid))}`;
    }
    return PATHS.notifications;
}
export const registerAppListeners = (): void => {
    startAppListening({
        matcher: isRejected,
        effect: (action, { dispatch }) => {
            const payload = action.payload as AppApiError | undefined;
            if (!payload)
                return;
            if (isNetworkError(payload)) {
                dispatch(toastsActions.push({ level: 'error', message: payload.message }));
                return;
            }
            if (isExternalSupplierError(payload)) {
                dispatch(toastsActions.push({
                    level: 'warning',
                    message: 'Внешний сервис недоступен, попробуйте позже',
                }));
                return;
            }
            if (fetchNotificationsThunk.rejected.match(action) ||
                fetchUnreadCountThunk.rejected.match(action) ||
                markNotificationReadThunk.rejected.match(action) ||
                markAllNotificationsReadThunk.rejected.match(action)) {
                dispatch(toastsActions.push({
                    level: 'error',
                    message: payload.message,
                    ttlMs: 5000,
                }));
            }
        },
    });
    startAppListening({
        actionCreator: loginThunk.fulfilled,
        effect: () => {
            eventsSocket.connect();
        },
    });
    startAppListening({
        actionCreator: restoreSessionThunk.fulfilled,
        effect: (action) => {
            if (action.payload)
                eventsSocket.connect();
        },
    });
    startAppListening({
        actionCreator: loginThunk.fulfilled,
        effect: (_, { dispatch }) => {
            void dispatch(fetchUnreadCountThunk());
        },
    });
    startAppListening({
        actionCreator: restoreSessionThunk.fulfilled,
        effect: (action, { dispatch }) => {
            if (action.payload)
                void dispatch(fetchUnreadCountThunk());
        },
    });
    startAppListening({
        actionCreator: authActions.logout,
        effect: () => {
            eventsSocket.disconnect();
        },
    });
    startAppListening({
        actionCreator: authActions.sessionExpired,
        effect: () => {
            eventsSocket.disconnect();
        },
    });
    let unsubscribeWs: (() => void) | null = null;
    startAppListening({
        matcher: isAnyOf(loginThunk.fulfilled, restoreSessionThunk.fulfilled),
        effect: (action, { dispatch }) => {
            if (restoreSessionThunk.fulfilled.match(action) && !action.payload) {
                return;
            }
            unsubscribeWs?.();
            unsubscribeWs = eventsSocket.subscribe((event) => {
                if (WS_TYPES_WITH_USER_MESSAGE.has(event.type) &&
                    typeof event.payload === 'object' &&
                    event.payload !== null) {
                    const payload = event.payload as {
                        id?: string;
                        createdAt?: number;
                        title?: string;
                        message?: string;
                        notificationId?: number;
                    };
                    if (typeof payload.message === 'string' && payload.message.trim()) {
                        const incoming: {
                            message: string;
                            id?: string;
                            createdAt?: number;
                            title?: string;
                        } = {
                            message: payload.message,
                        };
                        if (typeof payload.id === 'string')
                            incoming.id = payload.id;
                        else if (typeof payload.notificationId === 'number') {
                            incoming.id = String(payload.notificationId);
                        }
                        if (typeof payload.createdAt === 'number')
                            incoming.createdAt = payload.createdAt;
                        if (typeof payload.title === 'string')
                            incoming.title = payload.title;
                        dispatch(notificationsActions.received(incoming));
                        const record = event.payload as Record<string, unknown>;
                        const href = hrefForWsNotification(event.type, record);
                        dispatch(toastsActions.push({
                            level: 'info',
                            message: payload.title ? `${payload.title}: ${payload.message}` : payload.message,
                            ttlMs: 5000,
                            href,
                        }));
                        void dispatch(fetchUnreadCountThunk());
                    }
                }
                if (event.type === 'EVENT_UPDATED' && typeof event.payload === 'object' && event.payload) {
                    const payload = event.payload as {
                        eventId?: number;
                    };
                    if (typeof payload.eventId === 'number') {
                        void dispatch(fetchEventByIdThunk(payload.eventId as never));
                    }
                }
                if (event.type === 'TASK_UPDATED' && typeof event.payload === 'object' && event.payload) {
                    const payload = event.payload as {
                        taskId?: number;
                    };
                    if (typeof payload.taskId === 'number') {
                        void dispatch(fetchTaskByIdThunk(payload.taskId as never));
                    }
                }
            });
        },
    });
    startAppListening({
        actionCreator: authActions.logout,
        effect: () => {
            unsubscribeWs?.();
            unsubscribeWs = null;
        },
    });
};
