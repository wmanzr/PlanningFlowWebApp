package RUT.PlanningFlow.application.dto.resource;

import RUT.PlanningFlow.domain.enums.BookingStatus;
import RUT.PlanningFlow.domain.enums.ResourceType;

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
