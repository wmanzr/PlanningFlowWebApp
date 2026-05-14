package RUT.PlanningFlow.application.port.in.task;

import RUT.PlanningFlow.domain.vo.GeoPoint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UpdateTaskUseCase {
    Optional<Integer> execute(
            Integer callerUserId,
            Integer taskId,
            String newTitle,
            LocalDateTime newStartTime,
            LocalDateTime newEndTime,
            GeoPoint newLocation,
            List<Integer> requiredSkillIds,
            List<Integer> dependencyIds
    );
}
