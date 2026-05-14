package RUT.PlanningFlow.application.port.in.task;

import RUT.PlanningFlow.application.dto.task.TaskResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;

import java.time.LocalDateTime;

public interface GetMyTasksQuery {

    enum AssignmentFilter {
        
        ALL,
        CONFIRMED,
        NOT_CONFIRMED
    }

    PageResult<TaskResponseDto> execute(
            Integer callerUserId,
            Integer userId,
            AssignmentFilter filter,
            LocalDateTime start,
            LocalDateTime end,
            String title,
            PageQuery pageQuery
    );
}
