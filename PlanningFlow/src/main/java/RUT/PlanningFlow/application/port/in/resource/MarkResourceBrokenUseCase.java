package RUT.PlanningFlow.application.port.in.resource;

import java.util.Optional;

public interface MarkResourceBrokenUseCase {
    Optional<Integer> execute(Integer resourceId);
}