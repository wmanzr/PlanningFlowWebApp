package RUT.PlanningFlow.application.port.in.event;

import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.dto.event.EventResponseDto;

import java.time.LocalDateTime;

public interface ListEventsByDateRangeQuery {
    PageResult<EventResponseDto> execute(
            Integer callerUserId,
            LocalDateTime start,
            LocalDateTime end,
            String title,
            PageQuery pageQuery
    );
}