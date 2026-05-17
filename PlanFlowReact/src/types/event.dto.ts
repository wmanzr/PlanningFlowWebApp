import { type EventStatus } from './enums';
import { type EventId, type IsoDateTime, type UserId } from './common';
export interface EventResponseDto {
    id: EventId;
    title: string;
    description: string;
    status: EventStatus;
    startDate: IsoDateTime;
    endDate: IsoDateTime;
    latitude?: number;
    longitude?: number;
    creatorId?: UserId;
    coordinatorIds: UserId[];
    tasksCount?: number;
}
export interface EventPostMortemAiReportResponseDto {
    status: 'PENDING' | 'COMPLETED' | 'FAILED' | string;
    reportText: string | null;
    errorMessage: string | null;
    updatedAt: string | null;
}
export interface EventDashboardResponseDto {
    eventId: EventId;
    title: string;
    startDate: IsoDateTime;
    endDate: IsoDateTime;
    eventStatus: EventStatus;
    totalTasks: number;
    activeTasks: number;
    completedTasks: number;
    progressPercent: number;
    uniqueExecutorsCount: number;
    cancelledTasksCount: number;
    totalIncidentsCount: number;
}
export interface EventCreateRequest {
    title: string;
    description?: string;
    startDate: IsoDateTime;
    endDate: IsoDateTime;
    latitude?: number;
    longitude?: number;
}
export interface EventUpdateRequest {
    eventId: EventId;
    title?: string;
    description?: string;
    startDate?: IsoDateTime;
    endDate?: IsoDateTime;
    latitude?: number;
    longitude?: number;
    clearLocation?: boolean;
    coordinatorIds?: UserId[];
}
export interface EventCancelRequest {
    reason: string;
}
export interface ListEventsQuery {
    title?: string;
    start?: IsoDateTime;
    end?: IsoDateTime;
    page?: number;
    size?: number;
}
