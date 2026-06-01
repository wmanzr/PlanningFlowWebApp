package RUT.PlanningFlow.application.dto.task;

import RUT.PlanningFlow.domain.enums.AssignStatus;

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
