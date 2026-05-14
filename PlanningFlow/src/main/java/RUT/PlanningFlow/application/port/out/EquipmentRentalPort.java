package RUT.PlanningFlow.application.port.out;

import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.model.ExternalResource;
import RUT.PlanningFlow.domain.vo.DateTimeRange;

public interface EquipmentRentalPort {
    ExternalResource request(ResourceType type, String resourceName, DateTimeRange window);
}

