package RUT.PlanningFlow.application.port.out;

import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.model.InternalResource;
import RUT.PlanningFlow.domain.vo.DateTimeRange;

import java.util.List;

public interface InternalResourceWarehousePort {
    List<InternalResource> findAvailableOperationalByName(
            ResourceType type,
            String resourceName,
            DateTimeRange window,
            int limit
    );
}