package RUT.PlanningFlow.application.dto.event;

import RUT.PlanningFlow.domain.enums.EventStatus;

import java.time.LocalDateTime;
import java.util.List;

public final class EventResponseDto {
    private final Integer id;
    private final String title;
    private final String description;
    private final EventStatus status;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Double latitude;
    private final Double longitude;
    private final Integer creatorId;
    private final List<Integer> coordinatorIds;
    private final long tasksCount;

    public EventResponseDto(
            final Integer id,
            final String title,
            final String description,
            final EventStatus status,
            final LocalDateTime startDate,
            final LocalDateTime endDate,
            final Double latitude,
            final Double longitude,
            final Integer creatorId,
            final List<Integer> coordinatorIds,
            final long tasksCount
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.creatorId = creatorId;
        this.coordinatorIds = coordinatorIds == null ? List.of() : List.copyOf(coordinatorIds);
        this.tasksCount = tasksCount;
    }

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public EventStatus getStatus() { return status; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Integer getCreatorId() { return creatorId; }
    public List<Integer> getCoordinatorIds() { return coordinatorIds; }
    public long getTasksCount() { return tasksCount; }
}
