package RUT.PlanningFlow.domain.service.matching.model;

public enum RejectionReason {
    MISSING_REQUIRED_SKILLS,
    LATE_ARRIVAL,
    TIME_CONFLICT,
    DEADLINE_UNREACHABLE,
    DAILY_LOAD_EXCEEDED,
    OTHER
}