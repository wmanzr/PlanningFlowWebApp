package RUT.PlanningFlow.application.dto.event;

import RUT.PlanningFlow.domain.enums.EventStatus;

import java.time.LocalDateTime;

public record EventDashboardResponseDto(
        Integer eventId,
        String title,
        LocalDateTime startDate,
        LocalDateTime endDate,
        EventStatus eventStatus,
        int totalTasks,
        int activeTasks,
        int completedTasks,
        double progressPercent,
        int uniqueExecutorsCount,
        int cancelledTasksCount,
        int totalIncidentsCount
) {
}
