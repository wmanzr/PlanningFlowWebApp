package RUT.PlanningFlow.application.service.booking;

import RUT.PlanningFlow.application.dto.resource.ResourceBookingResponseDto;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.model.ExternalResource;
import RUT.PlanningFlow.domain.model.InternalResource;
import RUT.PlanningFlow.domain.model.Resource;
import RUT.PlanningFlow.domain.model.ResourceBooking;
import RUT.PlanningFlow.domain.model.Task;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public final class ResourceBookingResponseDtoMapper {

    public List<ResourceBookingResponseDto> toResponses(final List<ResourceBooking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return List.of();
        }
        final List<ResourceBookingResponseDto> dtos = new ArrayList<>(bookings.size());
        for (final ResourceBooking b : bookings) {
            if (b == null) {
                continue;
            }
            final ResourceBookingResponseDto dto = toResponse(b);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        return List.copyOf(dtos);
    }

    public ResourceBookingResponseDto toResponse(final ResourceBooking booking) {
        if (booking == null) {
            return null;
        }
        final Task task = booking.getTask();
        final Integer taskId = task == null ? null : task.getId();
        final Integer eventId;
        if (task == null || task.getEvent() == null) {
            eventId = null;
        } else {
            eventId = task.getEvent().getId();
        }

        final Resource resource = booking.getResource();
        final Integer resourceId = resource == null ? null : resource.getId();
        final String resourceName = resource == null ? null : resource.getName();
        final ResourceType resourceType = resource == null ? null : resource.getType();
        final String resourceSource;
        if (resource instanceof InternalResource) {
            resourceSource = "INTERNAL";
        } else if (resource instanceof ExternalResource) {
            resourceSource = "EXTERNAL";
        } else {
            resourceSource = resource == null ? null : "UNKNOWN";
        }

        return new ResourceBookingResponseDto(
                booking.getId(),
                taskId,
                eventId,
                resourceId,
                resourceName,
                resourceType,
                resourceSource,
                booking.getStatus(),
                booking.getReservedFrom(),
                booking.getReservedTo()
        );
    }
}
