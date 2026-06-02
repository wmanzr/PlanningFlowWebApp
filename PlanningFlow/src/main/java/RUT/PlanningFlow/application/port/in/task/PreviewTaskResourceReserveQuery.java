package RUT.PlanningFlow.application.port.in.task;

import RUT.PlanningFlow.application.dto.resource.ResourceReservePreviewDto;
import RUT.PlanningFlow.domain.enums.ResourceType;

import java.time.LocalDateTime;

public interface PreviewTaskResourceReserveQuery {

    ResourceReservePreviewDto preview(
            Integer callerUserId,
            Integer taskId,
            ResourceType resourceType,
            String resourceName,
            LocalDateTime reservedFrom,
            LocalDateTime reservedTo,
            int requiredCount
    );
}
