import { type AssignStatus, type TaskStatus, type MatchingMode } from './enums';
import { type AssignmentId, type EventId, type IsoDateTime, type SkillId, type TaskId, type UserId, } from './common';
export interface TaskAssignmentResponseDto {
    id: AssignmentId;
    userId: UserId;
    participantFullName: string;
    status: AssignStatus;
}
export interface TaskResponseDto {
    id: TaskId;
    eventId?: EventId;
    createdByUserId?: UserId;
    createdByFullName?: string | null;
    title: string;
    status: TaskStatus;
    startTime: IsoDateTime;
    endTime: IsoDateTime;
    latitude?: number;
    longitude?: number;
    requiredSkillIds: SkillId[];
    dependencyIds: TaskId[];
    assignments?: TaskAssignmentResponseDto[];
    requiredParticipantCount?: number | null;
    viewerAssignment?: TaskAssignmentResponseDto | null;
}
export interface TaskCreateRequest {
    eventId: EventId;
    title: string;
    startTime?: IsoDateTime;
    endTime?: IsoDateTime;
    latitude?: number;
    longitude?: number;
    requiredSkillIds?: SkillId[];
}
export interface TaskUpdateRequest {
    newTitle?: string;
    newStartTime?: IsoDateTime;
    newEndTime?: IsoDateTime;
    latitude?: number;
    longitude?: number;
    clearLocation?: boolean;
    requiredSkillIds?: SkillId[];
    dependencyIds?: TaskId[];
}
export interface TaskAssignRequest {
    userId: UserId;
}
export interface TaskMatchRequest {
    requiredCount: number;
    matchingMode: MatchingMode;
    geoReferenceRadiusMeters?: number;
    maxDailyLoadMinutes?: number;
    minTechnicalGapMinutes?: number;
}
export interface ListTasksForEventQuery {
    start?: IsoDateTime;
    end?: IsoDateTime;
    page?: number;
    size?: number;
}
