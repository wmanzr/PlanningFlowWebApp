package RUT.PlanningFlow.application.service.task;

import RUT.PlanningFlow.application.dto.task.TaskAssignmentResponseDto;
import RUT.PlanningFlow.application.dto.task.TaskResponseDto;
import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.model.Assignment;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public final class TaskResponseDtoMapper {

    public int countRequiredParticipantSlots(final List<Assignment> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return 0;
        }
        int n = 0;
        for (final Assignment a : assignments) {
            if (a == null || a.getStatus() == null) {
                continue;
            }
            final AssignStatus s = a.getStatus();
            if (s == AssignStatus.REJECTED || s == AssignStatus.CANCELLED) {
                continue;
            }
            n++;
        }
        return n;
    }

    public int countActiveAssignmentSlots(final List<TaskAssignmentResponseDto> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        int n = 0;
        for (final TaskAssignmentResponseDto row : rows) {
            if (row == null || row.getStatus() == null) {
                continue;
            }
            if (row.getStatus() == AssignStatus.REJECTED || row.getStatus() == AssignStatus.CANCELLED) {
                continue;
            }
            n++;
        }
        return n;
    }

    public TaskResponseDto toResponse(final Task task) {
        return toResponse(task, List.of(), null, null);
    }

    public TaskResponseDto toResponse(
            final Task task,
            final List<TaskAssignmentResponseDto> assignments,
            final Integer requiredParticipantCount
    ) {
        return toResponse(task, assignments, requiredParticipantCount, null);
    }

    public TaskResponseDto toResponse(
            final Task task,
            final List<TaskAssignmentResponseDto> assignments,
            final Integer requiredParticipantCount,
            final TaskAssignmentResponseDto viewerAssignment
    ) {
        if (task == null) {
            return null;
        }

        final List<Integer> requiredSkillIds = new ArrayList<>();
        for (final Skill s : task.getRequiredSkills()) {
            if (s != null && s.getId() != null) {
                requiredSkillIds.add(s.getId());
            }
        }

        final List<Integer> dependencyIds = new ArrayList<>();
        for (final Task d : task.getDependencies()) {
            if (d != null && d.getId() != null) {
                dependencyIds.add(d.getId());
            }
        }

        final User createdBy = task.getCreatedBy();
        final String createdByFullName = createdBy == null ? null : createdBy.getFullName();
        return new TaskResponseDto(
                task.getId(),
                task.getEvent() == null ? null : task.getEvent().getId(),
                createdBy == null ? null : createdBy.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getStartTime(),
                task.getEndTime(),
                task.getLatitude(),
                task.getLongitude(),
                requiredSkillIds,
                dependencyIds,
                assignments,
                requiredParticipantCount,
                createdByFullName,
                viewerAssignment
        );
    }
}
