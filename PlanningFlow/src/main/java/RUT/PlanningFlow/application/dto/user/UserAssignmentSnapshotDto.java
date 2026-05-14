package RUT.PlanningFlow.application.dto.user;

import java.time.LocalDateTime;

public record UserAssignmentSnapshotDto(
        Integer assignmentId,
        String assignmentStatus,
        LocalDateTime assignedAt,
        Integer taskId,
        String taskTitle,
        Integer eventId,
        String eventTitle,
        String eventStatus
) {
}
