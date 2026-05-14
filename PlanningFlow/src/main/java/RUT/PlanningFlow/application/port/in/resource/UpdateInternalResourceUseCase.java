package RUT.PlanningFlow.application.port.in.resource;

import RUT.PlanningFlow.domain.enums.ResourceType;

import java.util.Optional;

public interface UpdateInternalResourceUseCase {
    Optional<Integer> execute(
            Integer resourceId,
            String name,
            ResourceType type,
            String inventoryNumber
    );
}