package RUT.PlanningFlow.adapter.out.persistence.entity;

import RUT.PlanningFlow.domain.enums.IncidentSeverity;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
public class IncidentEntity extends BaseEntity implements Serializable {
    private EventEntity event;
    private TaskEntity task;
    private ResourceEntity resource;
    private UserEntity reporter;
    private String description;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;

    public IncidentEntity() {}

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    public EventEntity getEvent() { return event; }
    public void setEvent(EventEntity event) { this.event = event; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    public TaskEntity getTask() { return task; }
    public void setTask(TaskEntity task) { this.task = task; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    public ResourceEntity getResource() { return resource; }
    public void setResource(ResourceEntity resource) { this.resource = resource; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    public UserEntity getReporter() { return reporter; }
    public void setReporter(UserEntity reporter) { this.reporter = reporter; }

    @Column(columnDefinition = "TEXT", nullable = false)
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public IncidentSeverity getSeverity() { return severity; }
    public void setSeverity(IncidentSeverity severity) { this.severity = severity; }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }

    @Column(name = "created_at", nullable = false)
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Column(name = "resolved_at")
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
}