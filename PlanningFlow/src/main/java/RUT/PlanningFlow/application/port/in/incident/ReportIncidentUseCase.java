package RUT.PlanningFlow.application.port.in.incident;

import RUT.PlanningFlow.domain.enums.IncidentSeverity;

public interface ReportIncidentUseCase {
    Integer execute(Integer reporterId, Integer eventId, Integer taskId, Integer resourceId, String description, IncidentSeverity severity);
}