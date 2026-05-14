import type { RootState } from '@/store/types';
export const selectNotifications = (state: RootState) => state.notifications.items;
export const selectUnreadNotificationsCount = (state: RootState) => state.notifications.unreadCount;
export const selectNotificationsListMeta = (state: RootState) => ({
    status: state.notifications.listStatus,
    error: state.notifications.listError,
    totalPages: state.notifications.totalPages,
    totalElements: state.notifications.totalElements,
});
