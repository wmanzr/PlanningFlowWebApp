package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.IncidentSeverity;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.utils.DomainAssert;

import java.time.LocalDateTime;

public class Incident {
    private final Integer id;
    private final Event event;
    private Task task;
    private Resource resource;
    private User reporter;
    private String description;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;

    public Incident(
            final Integer id,
            final Event event,
            final Task task,
            final Resource resource,
            final User reporter,
            final String description,
            final IncidentSeverity severity,
            final IncidentStatus status,
            final LocalDateTime createdAt,
            final LocalDateTime resolvedAt,
            final String resolutionNotes
    ) {
        this.id = id;
        DomainAssert.notNull(event, "Мероприятие инцидента обязательно", "EVENT_REQUIRED");
        this.event = event;
        this.task = task;
        this.resource = resource;
        DomainAssert.notNull(reporter, "Инициатор инцидента обязателен", "INCIDENT_REPORTER_REQUIRED");
        this.reporter = reporter;
        DomainAssert.notBlank(description, "Описание инцидента обязательно", "INCIDENT_DESCRIPTION_REQUIRED");
        this.description = description;
        DomainAssert.notNull(severity, "Критичность инцидента обязательна", "INCIDENT_SEVERITY_REQUIRED");
        DomainAssert.notNull(status, "Статус инцидента обязателен", "INCIDENT_STATUS_REQUIRED");
        this.severity = severity;
        this.status = status;
        DomainAssert.notNull(createdAt, "Время создания инцидента обязательно", "INCIDENT_CREATED_AT_REQUIRED");
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
        this.resolutionNotes = resolutionNotes;
    }

    public void updateDescription(final String newDescription) {
        DomainAssert.notBlank(newDescription, "Описание инцидента обязательно", "INCIDENT_DESCRIPTION_REQUIRED");
        this.description = newDescription;
    }

    public void changeSeverity(final IncidentSeverity newSeverity) {
        DomainAssert.notNull(newSeverity, "Критичность инцидента обязательна", "INCIDENT_SEVERITY_REQUIRED");
        this.severity = newSeverity;
    }

    public void markAsInProgress() {
        if (this.status == IncidentStatus.IN_PROGRESS) {
            return;
        }
        if (this.status != IncidentStatus.OPEN) {
            throw new DomainException("Инцидент можно взять в работу только из статуса OPEN", "INVALID_INCIDENT_STATE");
        }
        this.status = IncidentStatus.IN_PROGRESS;
    }

    public void resolve(final String resolutionNotes) {
        if (this.status == IncidentStatus.RESOLVED) {
            return;
        }
        DomainAssert.notBlank(resolutionNotes, "Описание решения обязательно", "INCIDENT_RESOLUTION_NOTES_REQUIRED");
        this.status = IncidentStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = resolutionNotes;
    }

    public Integer getId() { return id; }
    public Event getEvent() { return event; }
    public Task getTask() { return task; }
    public Resource getResource() { return resource; }
    public User getReporter() { return reporter; }
    public String getDescription() { return description; }
    public IncidentSeverity getSeverity() { return severity; }
    public IncidentStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public String getResolutionNotes() { return resolutionNotes; }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Incident that = (Incident) o;
        return id != null && that.id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}