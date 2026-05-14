declare const brand: unique symbol;
type Brand<T, B> = T & {
    readonly [brand]: B;
};
export type IsoDateTime = Brand<string, 'IsoDateTime'>;
export type EventId = Brand<number, 'EventId'>;
export type TaskId = Brand<number, 'TaskId'>;
export type UserId = Brand<number, 'UserId'>;
export type SkillId = Brand<number, 'SkillId'>;
export type ResourceId = Brand<number, 'ResourceId'>;
export type BookingId = Brand<number, 'BookingId'>;
export type IncidentId = Brand<number, 'IncidentId'>;
export type AssignmentId = Brand<number, 'AssignmentId'>;
export const asEventId = (n: number): EventId => n as EventId;
export const asTaskId = (n: number): TaskId => n as TaskId;
export const asUserId = (n: number): UserId => n as UserId;
export const asSkillId = (n: number): SkillId => n as SkillId;
export const asResourceId = (n: number): ResourceId => n as ResourceId;
export const asBookingId = (n: number): BookingId => n as BookingId;
export const asIncidentId = (n: number): IncidentId => n as IncidentId;
export const asAssignmentId = (n: number): AssignmentId => n as AssignmentId;
export const asIsoDateTime = (s: string): IsoDateTime => s as IsoDateTime;
export interface PageResult<T> {
    items: T[];
    totalElements: number;
    totalPages: number;
}
export interface PageQuery {
    page?: number;
    size?: number;
}
export interface DateRangeQuery {
    start?: IsoDateTime;
    end?: IsoDateTime;
}
export interface ApiErrorResponse {
    message: string;
    errorCode: string;
    timestamp: number;
    fieldErrors?: Record<string, string>;
}
export type AsyncStatus = 'idle' | 'pending' | 'succeeded' | 'failed';
export interface AppApiError {
    message: string;
    errorCode: string;
    httpStatus: number | null;
    timestamp: number;
    fieldErrors?: Record<string, string>;
}
