package RUT.PlanningFlow.application.port.in.booking;

import RUT.PlanningFlow.application.dto.resource.ResourceBookingResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;

public interface ListResourceBookingsForTaskQuery {
    PageResult<ResourceBookingResponseDto> execute(Integer taskId, PageQuery pageQuery);
}