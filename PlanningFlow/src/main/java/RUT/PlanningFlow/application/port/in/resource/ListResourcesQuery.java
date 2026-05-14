package RUT.PlanningFlow.application.port.in.resource;

import RUT.PlanningFlow.application.dto.resource.InternalResourceResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;

public interface ListResourcesQuery {
    PageResult<InternalResourceResponseDto> execute(String name, PageQuery pageQuery);
}