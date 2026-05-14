package RUT.PlanningFlow.application.dto.user;


public record UserProfileActivityStatsDto(
        int completedTasksCount,
        int eventsParticipatedCount,
        double totalWorkedHours,
        int coordinatorCompletedEventsCount,
        int coordinatorTasksCreatedCount,
        int coordinatorBookingsCreatedCount,
        int organizerEventsCreatedCount,
        int organizerTasksCreatedCount,
        int organizerBookingsCreatedCount
) {
    public static UserProfileActivityStatsDto zero() {
        return new UserProfileActivityStatsDto(0, 0, 0.0, 0, 0, 0, 0, 0, 0);
    }
}
