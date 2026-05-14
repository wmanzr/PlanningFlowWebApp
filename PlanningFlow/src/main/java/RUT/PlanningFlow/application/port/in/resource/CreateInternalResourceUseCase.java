package RUT.PlanningFlow.application.port.in.resource;

import RUT.PlanningFlow.domain.enums.ResourceType;

public interface CreateInternalResourceUseCase {
    Integer execute(String name, ResourceType type, String inventoryNumber);
}