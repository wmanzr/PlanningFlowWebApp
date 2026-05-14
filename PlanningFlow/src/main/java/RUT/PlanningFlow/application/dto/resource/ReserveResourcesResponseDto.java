package RUT.PlanningFlow.application.dto.resource;

import RUT.PlanningFlow.domain.enums.ResourceType;

import java.util.List;

public record ReserveResourcesResponseDto(
        Integer taskId,
        ResourceType resourceType,
        int requiredCount,
        List<ResourceBookingResponseDto> createdBookings
) {
    public ReserveResourcesResponseDto {
        createdBookings = createdBookings == null ? List.of() : List.copyOf(createdBookings);
    }
}
