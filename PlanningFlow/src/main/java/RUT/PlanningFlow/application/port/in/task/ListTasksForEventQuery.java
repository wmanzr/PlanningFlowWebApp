package RUT.PlanningFlow.application.port.in.task;

import RUT.PlanningFlow.application.dto.task.TaskResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;

import java.time.LocalDateTime;

public interface ListTasksForEventQuery {
    PageResult<TaskResponseDto> execute(Integer callerUserId, Integer eventId, LocalDateTime start, LocalDateTime end, PageQuery pageQuery);
}
