package RUT.PlanningFlow.application.port.in.incident;

import RUT.PlanningFlow.application.dto.incident.IncidentResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;

public interface GetIncidentsForEventQuery {
    PageResult<IncidentResponseDto> execute(Integer eventId, PageQuery pageQuery);
}