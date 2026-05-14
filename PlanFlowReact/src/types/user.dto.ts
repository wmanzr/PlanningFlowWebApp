import { type SkillTier, type UserRole } from './enums';
import { type AssignmentId, type IsoDateTime, type SkillId, type UserId, } from './common';
export type IsoDate = string;
export interface UserResponseDto {
    id: UserId;
    username: string;
    email: string;
    fullName: string;
    birthDate: IsoDate;
    roles: UserRole[];
    completedTasksCount: number;
    eventsParticipatedCount: number;
    totalWorkedHours: number;
    coordinatorCompletedEventsCount: number;
    coordinatorTasksCreatedCount: number;
    coordinatorBookingsCreatedCount: number;
    organizerEventsCreatedCount: number;
    organizerTasksCreatedCount: number;
    organizerBookingsCreatedCount: number;
}
export interface UserSkillResponseDto {
    userSkillId: number;
    skillId?: SkillId;
    skillName?: string;
    tier: SkillTier;
    verifiedAt?: IsoDateTime;
}
export interface UserProfileUpdateRequest {
    fullName?: string;
}
export interface UserSkillTierItem {
    skillId: SkillId;
    tier: SkillTier;
}
export interface UserSkillsUpdateRequest {
    skillTiers: UserSkillTierItem[];
}
export interface AssignmentRejectRequest {
    reason: string;
}
export interface AssignmentActionRef {
    assignmentId: AssignmentId;
}
export interface UserAssignmentSnapshotDto {
    assignmentId: number;
    assignmentStatus: string;
    assignedAt: string;
    taskId: number;
    taskTitle: string;
    eventId: number;
    eventTitle: string;
    eventStatus: string;
}
export interface EventBriefViewerDto {
    eventId: number;
    title: string;
    status: string;
    startDate: string;
    endDate: string;
}
export interface UserViewerContextDto {
    adminAllAssignments: UserAssignmentSnapshotDto[];
    organizerParticipantAssignments: UserAssignmentSnapshotDto[];
    organizerCoordinatorEvents: EventBriefViewerDto[];
    completedEventsAsOrganizerCount: number | null;
    coordinatorEventsUnderOrganizerCount: number | null;
    coordinatorEventsTotalCount: number | null;
    participantEventsUnderViewerCount: number | null;
    participantEventsTotalCount: number | null;
}
