package RUT.PlanningFlow.application.port.in.task;

import RUT.PlanningFlow.application.dto.resource.ReserveResourcesResponseDto;
import RUT.PlanningFlow.domain.enums.ResourceType;

import java.time.LocalDateTime;

public interface AllocateTaskResourcesUseCase {
    ReserveResourcesResponseDto execute(
            Integer callerUserId,
            Integer taskId,
            ResourceType type,
            String resourceName,
            int requiredCount,
            LocalDateTime reservedFrom,
            LocalDateTime reservedTo
    );
}
