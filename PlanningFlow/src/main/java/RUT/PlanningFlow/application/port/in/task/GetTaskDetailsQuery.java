package RUT.PlanningFlow.application.port.in.task;

import RUT.PlanningFlow.application.dto.task.TaskResponseDto;

import java.util.Optional;

public interface GetTaskDetailsQuery {
    Optional<TaskResponseDto> execute(Integer callerUserId, Integer taskId);
}
