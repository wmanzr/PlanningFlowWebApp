package RUT.PlanningFlow.application.dto.task;

import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class TaskResponseDto {
    private final Integer id;
    private final Integer eventId;
    private final Integer createdByUserId;
    private final String title;
    private final TaskStatus status;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Double latitude;
    private final Double longitude;
    private final List<Integer> requiredSkillIds;
    private final List<Integer> dependencyIds;
    private final List<TaskAssignmentResponseDto> assignments;
    private final Integer requiredParticipantCount;
    private final String createdByFullName;
    
    private final TaskAssignmentResponseDto viewerAssignment;

    public TaskResponseDto(
            final Integer id,
            final Integer eventId,
            final Integer createdByUserId,
            final String title,
            final TaskStatus status,
            final LocalDateTime startTime,
            final LocalDateTime endTime,
            final Double latitude,
            final Double longitude,
            final List<Integer> requiredSkillIds,
            final List<Integer> dependencyIds,
            final List<TaskAssignmentResponseDto> assignments,
            final Integer requiredParticipantCount,
            final String createdByFullName,
            final TaskAssignmentResponseDto viewerAssignment
    ) {
        this.id = id;
        this.eventId = eventId;
        this.createdByUserId = createdByUserId;
        this.title = title;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.requiredSkillIds = requiredSkillIds == null ? List.of() : List.copyOf(requiredSkillIds);
        this.dependencyIds = dependencyIds == null ? List.of() : List.copyOf(dependencyIds);
        this.assignments = assignments == null ? List.of() : List.copyOf(assignments);
        this.requiredParticipantCount = requiredParticipantCount;
        this.createdByFullName = createdByFullName;
        this.viewerAssignment = viewerAssignment;
    }

    public static TaskResponseDto from(final Task task) {
        return from(task, List.of(), null, null);
    }

    public static TaskResponseDto from(
            final Task task,
            final List<TaskAssignmentResponseDto> assignments,
            final Integer requiredParticipantCount
    ) {
        return from(task, assignments, requiredParticipantCount, null);
    }

    public static TaskResponseDto from(
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

    public Integer getId() {
        return id;
    }

    public Integer getEventId() {
        return eventId;
    }

    public Integer getCreatedByUserId() {
        return createdByUserId;
    }

    public String getTitle() {
        return title;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public List<Integer> getRequiredSkillIds() {
        return requiredSkillIds;
    }

    public List<Integer> getDependencyIds() {
        return dependencyIds;
    }

    public List<TaskAssignmentResponseDto> getAssignments() {
        return assignments;
    }

    public Integer getRequiredParticipantCount() {
        return requiredParticipantCount;
    }

    public String getCreatedByFullName() {
        return createdByFullName;
    }

    public TaskAssignmentResponseDto getViewerAssignment() {
        return viewerAssignment;
    }

    public static int countActiveAssignmentSlots(final List<TaskAssignmentResponseDto> rows) {
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
}
