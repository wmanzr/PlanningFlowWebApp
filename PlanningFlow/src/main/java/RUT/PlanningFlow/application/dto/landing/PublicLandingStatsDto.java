package RUT.PlanningFlow.application.dto.landing;

public record PublicLandingStatsDto(
        long totalEventsCount,
        long completedEventsCount,
        long tasksDoneCount,
        long registeredUsersCount,
        long resolvedIncidentsCount,
        long acceptedAssignmentsCount
) {
}
