import { type IncidentSeverity, type IncidentStatus } from './enums';
import { type EventId, type IncidentId, type IsoDateTime, type ResourceId, type TaskId, type UserId, } from './common';
export interface IncidentResponseDto {
    id: IncidentId;
    eventId?: EventId;
    taskId?: TaskId;
    resourceId?: ResourceId;
    reporterUserId?: UserId;
    description: string;
    severity: IncidentSeverity;
    status: IncidentStatus;
    createdAt: IsoDateTime;
    resolvedAt?: IsoDateTime;
    resolutionNotes?: string;
}
export interface IncidentCreateRequest {
    reporterId: UserId;
    eventId: EventId;
    taskId?: TaskId;
    resourceId?: ResourceId;
    description: string;
    severity: IncidentSeverity;
}
export interface IncidentResolveRequest {
    resolutionNotes: string;
}
