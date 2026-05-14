package RUT.PlanningFlow.application.dto.resource;

import RUT.PlanningFlow.domain.enums.BookingStatus;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.model.ExternalResource;
import RUT.PlanningFlow.domain.model.InternalResource;
import RUT.PlanningFlow.domain.model.Resource;
import RUT.PlanningFlow.domain.model.ResourceBooking;
import RUT.PlanningFlow.domain.model.Task;

import java.time.LocalDateTime;

public final class ResourceBookingResponseDto {
    private final Integer id;
    private final Integer taskId;
    private final Integer eventId;
    private final Integer resourceId;
    private final String resourceName;
    private final ResourceType resourceType;
    private final String resourceSource;
    private final BookingStatus status;
    private final LocalDateTime reservedFrom;
    private final LocalDateTime reservedTo;

    public ResourceBookingResponseDto(
            final Integer id,
            final Integer taskId,
            final Integer eventId,
            final Integer resourceId,
            final String resourceName,
            final ResourceType resourceType,
            final String resourceSource,
            final BookingStatus status,
            final LocalDateTime reservedFrom,
            final LocalDateTime reservedTo
    ) {
        this.id = id;
        this.taskId = taskId;
        this.eventId = eventId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.resourceSource = resourceSource;
        this.status = status;
        this.reservedFrom = reservedFrom;
        this.reservedTo = reservedTo;
    }

    public static ResourceBookingResponseDto from(final ResourceBooking booking) {
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

    public Integer getId() {
        return id;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public Integer getEventId() {
        return eventId;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public String getResourceSource() {
        return resourceSource;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public LocalDateTime getReservedFrom() {
        return reservedFrom;
    }

    public LocalDateTime getReservedTo() {
        return reservedTo;
    }
}
