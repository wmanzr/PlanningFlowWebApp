import { http } from '../http';
import { ENDPOINTS } from '../endpoints';
import type { PageQuery, PageResult } from '@/types';
export interface NotificationDto {
    id: number;
    title: string | null;
    message: string;
    createdAt: string;
    readAt: string | null;
}
export interface ListNotificationsQuery extends PageQuery {
    filter?: 'all' | 'unread' | 'read';
}
export const notificationsApi = {
    list: (query: ListNotificationsQuery): Promise<PageResult<NotificationDto>> => http
        .get<PageResult<NotificationDto>>(ENDPOINTS.notifications.root, { params: query })
        .then((r) => r.data),
    unreadCount: (): Promise<number> => http.get<number>(ENDPOINTS.notifications.unreadCount).then((r) => r.data),
    markRead: (id: number): Promise<void> => http.post<void>(ENDPOINTS.notifications.read(id)).then(() => undefined),
    markAllRead: (): Promise<void> => http.post<void>(ENDPOINTS.notifications.readAll).then(() => undefined),
};
