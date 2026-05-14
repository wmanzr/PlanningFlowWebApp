export const EventStatus = {
    DRAFT: 'DRAFT',
    PLANNING: 'PLANNING',
    ACTIVE: 'ACTIVE',
    COMPLETED: 'COMPLETED',
    CANCELLED: 'CANCELLED',
} as const;
export type EventStatus = (typeof EventStatus)[keyof typeof EventStatus];
export const TaskStatus = {
    OPEN: 'OPEN',
    ASSIGNED: 'ASSIGNED',
    IN_PROGRESS: 'IN_PROGRESS',
    DONE: 'DONE',
    CANCELLED: 'CANCELLED',
} as const;
export type TaskStatus = (typeof TaskStatus)[keyof typeof TaskStatus];
export const AssignStatus = {
    PENDING: 'PENDING',
    ACCEPTED: 'ACCEPTED',
    REJECTED: 'REJECTED',
    CANCELLED: 'CANCELLED',
} as const;
export type AssignStatus = (typeof AssignStatus)[keyof typeof AssignStatus];
export const AssignmentFilter = {
    ALL: 'ALL',
    CONFIRMED: 'CONFIRMED',
    NOT_CONFIRMED: 'NOT_CONFIRMED',
} as const;
export type AssignmentFilter = (typeof AssignmentFilter)[keyof typeof AssignmentFilter];
export const BookingStatus = {
    REQUESTED: 'REQUESTED',
    CONFIRMED: 'CONFIRMED',
    FAILED: 'FAILED',
    CANCELLED: 'CANCELLED',
} as const;
export type BookingStatus = (typeof BookingStatus)[keyof typeof BookingStatus];
export const ResourceType = {
    EQUIPMENT: 'EQUIPMENT',
    TRANSPORT: 'TRANSPORT',
    MATERIAL: 'MATERIAL',
} as const;
export type ResourceType = (typeof ResourceType)[keyof typeof ResourceType];
export const ResourceSource = {
    INTERNAL: 'INTERNAL',
    EXTERNAL: 'EXTERNAL',
    UNKNOWN: 'UNKNOWN',
} as const;
export type ResourceSource = (typeof ResourceSource)[keyof typeof ResourceSource];
export const IncidentStatus = {
    OPEN: 'OPEN',
    IN_PROGRESS: 'IN_PROGRESS',
    RESOLVED: 'RESOLVED',
} as const;
export type IncidentStatus = (typeof IncidentStatus)[keyof typeof IncidentStatus];
export const IncidentSeverity = {
    LOW: 'LOW',
    MEDIUM: 'MEDIUM',
    HIGH: 'HIGH',
    CRITICAL: 'CRITICAL',
} as const;
export type IncidentSeverity = (typeof IncidentSeverity)[keyof typeof IncidentSeverity];
export const MatchingMode = {
    STANDARD: 'STANDARD',
    CRITICAL: 'CRITICAL',
} as const;
export type MatchingMode = (typeof MatchingMode)[keyof typeof MatchingMode];
export const SkillTier = {
    NOVICE: 'NOVICE',
    PRACTITIONER: 'PRACTITIONER',
    EXPERT: 'EXPERT',
} as const;
export type SkillTier = (typeof SkillTier)[keyof typeof SkillTier];
export const UserRole = {
    ORGANIZER: 'ORGANIZER',
    COORDINATOR: 'COORDINATOR',
    PARTICIPANT: 'PARTICIPANT',
    ADMIN: 'ADMIN',
} as const;
export type UserRole = (typeof UserRole)[keyof typeof UserRole];
export const RejectionReason = {
    MISSING_REQUIRED_SKILLS: 'MISSING_REQUIRED_SKILLS',
    LATE_ARRIVAL: 'LATE_ARRIVAL',
    TIME_CONFLICT: 'TIME_CONFLICT',
    DEADLINE_UNREACHABLE: 'DEADLINE_UNREACHABLE',
    DAILY_LOAD_EXCEEDED: 'DAILY_LOAD_EXCEEDED',
    OTHER: 'OTHER',
} as const;
export type RejectionReason = (typeof RejectionReason)[keyof typeof RejectionReason];
