package RUT.PlanningFlow.application.port.in.incident;

import RUT.PlanningFlow.application.dto.incident.IncidentResponseDto;

import java.util.Optional;

public interface GetIncidentDetailsQuery {
    Optional<IncidentResponseDto> execute(Integer incidentId);
}