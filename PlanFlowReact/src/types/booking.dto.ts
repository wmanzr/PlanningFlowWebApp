import { type BookingStatus, type ResourceSource, type ResourceType } from './enums';
import { type BookingId, type EventId, type IsoDateTime, type ResourceId, type TaskId, } from './common';
export interface ResourceBookingResponseDto {
    id: BookingId;
    taskId?: TaskId;
    eventId?: EventId;
    resourceId?: ResourceId;
    resourceName?: string;
    resourceType?: ResourceType;
    resourceSource?: ResourceSource;
    status: BookingStatus;
    reservedFrom: IsoDateTime;
    reservedTo: IsoDateTime;
}
export interface ResourceBookingRescheduleRequest {
    reservedFrom: IsoDateTime;
    reservedTo: IsoDateTime;
}
export interface ReserveResourcesResponseDto {
    taskId: TaskId;
    resourceType: ResourceType;
    requiredCount: number;
    createdBookings: ResourceBookingResponseDto[];
}
export interface TaskAllocateResourcesRequest {
    resourceType: ResourceType;
    resourceName: string;
    requiredCount: number;
    reservedFrom: IsoDateTime;
    reservedTo: IsoDateTime;
}
