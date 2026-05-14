package RUT.PlanningFlow.application.port.in.task;

import RUT.PlanningFlow.domain.vo.GeoPoint;

import java.time.LocalDateTime;
import java.util.List;

public interface CreateTaskUseCase {
    Integer execute(
            Integer callerUserId,
            Integer eventId,
            String title,
            LocalDateTime startTime,
            LocalDateTime endTime,
            GeoPoint location,
            List<Integer> requiredSkillIds
    );
}
