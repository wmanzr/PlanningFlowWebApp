package RUT.PlanningFlow.application.port.in.event;

import java.util.Optional;

public interface ManageEventStatusUseCase {
    Optional<Integer> startPlanning(Integer callerUserId, Integer eventId);
    Optional<Integer> activate(Integer callerUserId, Integer eventId);
    Optional<Integer> complete(Integer callerUserId, Integer eventId);
    Optional<Integer> cancel(Integer callerUserId, Integer eventId, String reason);
}
