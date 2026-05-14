package RUT.PlanningFlow.adapter.out.persistence.entity;

import RUT.PlanningFlow.domain.enums.EventAiPostmortemReportStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_ai_postmortem_reports")
public class EventAiPostmortemReportEntity extends BaseEntity implements Serializable {

    private EventEntity event;
    private EventAiPostmortemReportStatus status;
    private String reportText;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EventAiPostmortemReportEntity() {
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, unique = true)
    public EventEntity getEvent() {
        return event;
    }

    public void setEvent(final EventEntity event) {
        this.event = event;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    public EventAiPostmortemReportStatus getStatus() {
        return status;
    }

    public void setStatus(final EventAiPostmortemReportStatus status) {
        this.status = status;
    }

    @Column(columnDefinition = "TEXT")
    public String getReportText() {
        return reportText;
    }

    public void setReportText(final String reportText) {
        this.reportText = reportText;
    }

    @Column(length = 4000)
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Column(name = "created_at", nullable = false)
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
