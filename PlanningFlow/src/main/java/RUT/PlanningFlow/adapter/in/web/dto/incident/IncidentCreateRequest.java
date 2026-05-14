package RUT.PlanningFlow.adapter.in.web.dto.incident;

import RUT.PlanningFlow.domain.enums.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class IncidentCreateRequest {

    @NotNull(message = "ID инициатора обязателен")
    @Positive(message = "Идентификатор инициатора должен быть положительным")
    private Integer reporterId;

    @NotNull(message = "ID мероприятия обязателен")
    @Positive(message = "Идентификатор мероприятия должен быть положительным")
    private Integer eventId;

    @Positive(message = "Идентификатор задачи должен быть положительным")
    private Integer taskId;

    @Positive(message = "Идентификатор ресурса должен быть положительным")
    private Integer resourceId;

    @NotBlank(message = "Описание инцидента обязательно")
    @Size(max = 65535, message = "Описание слишком длинное")
    private String description;

    @NotNull(message = "Критичность инцидента обязательна")
    private IncidentSeverity severity;

    public IncidentCreateRequest() {
    }

    public Integer getReporterId() {
        return reporterId;
    }

    public void setReporterId(final Integer reporterId) {
        this.reporterId = reporterId;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(final Integer eventId) {
        this.eventId = eventId;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(final Integer taskId) {
        this.taskId = taskId;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(final Integer resourceId) {
        this.resourceId = resourceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(final IncidentSeverity severity) {
        this.severity = severity;
    }
}
