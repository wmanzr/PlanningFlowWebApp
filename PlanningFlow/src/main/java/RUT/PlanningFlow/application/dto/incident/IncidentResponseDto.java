package RUT.PlanningFlow.application.dto.incident;

import RUT.PlanningFlow.domain.enums.IncidentSeverity;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Incident;
import RUT.PlanningFlow.domain.model.Resource;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;

import java.time.LocalDateTime;

public final class IncidentResponseDto {
    private final Integer id;
    private final Integer eventId;
    private final Integer taskId;
    private final Integer resourceId;
    private final Integer reporterUserId;
    private final String description;
    private final IncidentSeverity severity;
    private final IncidentStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime resolvedAt;
    private final String resolutionNotes;

    public IncidentResponseDto(
            final Integer id,
            final Integer eventId,
            final Integer taskId,
            final Integer resourceId,
            final Integer reporterUserId,
            final String description,
            final IncidentSeverity severity,
            final IncidentStatus status,
            final LocalDateTime createdAt,
            final LocalDateTime resolvedAt,
            final String resolutionNotes
    ) {
        this.id = id;
        this.eventId = eventId;
        this.taskId = taskId;
        this.resourceId = resourceId;
        this.reporterUserId = reporterUserId;
        this.description = description;
        this.severity = severity;
        this.status = status;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
        this.resolutionNotes = resolutionNotes;
    }

    public static IncidentResponseDto from(final Incident incident) {
        if (incident == null) {
            return null;
        }

        final Event event = incident.getEvent();
        final Task task = incident.getTask();
        final Resource resource = incident.getResource();
        final User reporter = incident.getReporter();

        return new IncidentResponseDto(
                incident.getId(),
                event == null ? null : event.getId(),
                task == null ? null : task.getId(),
                resource == null ? null : resource.getId(),
                reporter == null ? null : reporter.getId(),
                incident.getDescription(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getCreatedAt(),
                incident.getResolvedAt(),
                incident.getResolutionNotes()
        );
    }

    public Integer getId() { return id; }
    public Integer getEventId() { return eventId; }
    public Integer getTaskId() { return taskId; }
    public Integer getResourceId() { return resourceId; }
    public Integer getReporterUserId() { return reporterUserId; }
    public String getDescription() { return description; }
    public IncidentSeverity getSeverity() { return severity; }
    public IncidentStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public String getResolutionNotes() { return resolutionNotes; }
}