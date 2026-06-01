package RUT.PlanningFlow.application.service.task;

import RUT.PlanningFlow.application.dto.task.TaskAssignmentResponseDto;
import RUT.PlanningFlow.domain.model.Assignment;
import org.springframework.stereotype.Component;

@Component
public final class TaskAssignmentResponseDtoMapper {

    public TaskAssignmentResponseDto toResponse(final Assignment assignment) {
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
}
