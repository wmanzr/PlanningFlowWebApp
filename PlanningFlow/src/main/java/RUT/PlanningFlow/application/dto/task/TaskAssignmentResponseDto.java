package RUT.PlanningFlow.application.dto.task;

import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.model.Assignment;

public final class TaskAssignmentResponseDto {
    private final Integer id;
    private final Integer userId;
    private final String participantFullName;
    private final AssignStatus status;

    public TaskAssignmentResponseDto(
            final Integer id,
            final Integer userId,
            final String participantFullName,
            final AssignStatus status
    ) {
        this.id = id;
        this.userId = userId;
        this.participantFullName = participantFullName;
        this.status = status;
    }

    public static TaskAssignmentResponseDto from(final Assignment assignment) {
        if (assignment == null || assignment.getUser() == null) {
            return null;
        }
        final String name = assignment.getUser().getFullName();
        return new TaskAssignmentResponseDto(
                assignment.getId(),
                assignment.getUser().getId(),
                name,
                assignment.getStatus()
        );
    }

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getParticipantFullName() {
        return participantFullName;
    }

    public AssignStatus getStatus() {
        return status;
    }
}
