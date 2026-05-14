package RUT.PlanningFlow.application.port.in.resource;

import java.util.Optional;

public interface MarkResourceOperationalUseCase {
    Optional<Integer> execute(Integer resourceId);
}