package RUT.PlanningFlow.application.port.in.task;

import java.util.Optional;

public interface ManageTaskStatusUseCase {
    Optional<Integer> startExecution(Integer callerUserId, Integer taskId);
    Optional<Integer> markAsDone(Integer callerUserId, Integer taskId);
    Optional<Integer> cancel(Integer callerUserId, Integer taskId);
}
