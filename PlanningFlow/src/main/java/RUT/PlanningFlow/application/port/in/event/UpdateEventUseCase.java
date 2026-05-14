package RUT.PlanningFlow.application.port.in.event;

import RUT.PlanningFlow.domain.vo.GeoPoint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UpdateEventUseCase {
    Optional<Integer> execute(
            Integer callerUserId,
            Integer eventId,
            String newTitle,
            String newDescription,
            LocalDateTime newStartDate,
            LocalDateTime newEndDate,
            GeoPoint newLocation,
            List<Integer> coordinatorIds
    );
}
