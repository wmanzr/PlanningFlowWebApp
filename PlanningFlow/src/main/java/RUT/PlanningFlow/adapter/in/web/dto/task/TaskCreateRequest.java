package RUT.PlanningFlow.adapter.in.web.dto.task;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskCreateRequest {

    @NotNull(message = "ID мероприятия обязателен")
    @Positive(message = "ID мероприятия должен быть положительным")
    private Integer eventId;

    @NotBlank(message = "Название задачи обязательно")
    @Size(max = 2048, message = "Название слишком длинное")
    private String title;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @DecimalMin(value = "-90.0", inclusive = true, message = "Широта должна быть не меньше -90")
    @DecimalMax(value = "90.0", inclusive = true, message = "Широта должна быть не больше 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", inclusive = true, message = "Долгота должна быть не меньше -180")
    @DecimalMax(value = "180.0", inclusive = true, message = "Долгота должна быть не больше 180")
    private Double longitude;

    private List<@Positive(message = "ID навыка должен быть положительным") Integer> requiredSkillIds =
            new ArrayList<>();

    public TaskCreateRequest() {
    }

    @AssertTrue(message = "Укажите оба времени или не указывайте ни одного — будут использованы границы мероприятия")
    boolean isSchedulePairConsistent() {
        if (startTime == null && endTime == null) {
            return true;
        }
        return startTime != null && endTime != null;
    }

    @AssertTrue(message = "Некорректный интервал: начало не может быть позже окончания")
    boolean isValidDateRange() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return !startTime.isAfter(endTime);
    }

    @AssertTrue(message = "Задача не может длиться больше 8 часов")
    boolean isScheduleWithinMaxDuration() {
        if (startTime == null || endTime == null) {
            return true;
        }
        final Duration duration = Duration.between(startTime, endTime);
        return !duration.minusHours(8).isPositive();
    }

    @AssertTrue(message = "Широта и долгота задаются вместе")
    boolean isGeoPairConsistent() {
        if (latitude == null && longitude == null) {
            return true;
        }
        return latitude != null && longitude != null;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(final Integer eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(final LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(final LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(final Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(final Double longitude) {
        this.longitude = longitude;
    }

    public List<Integer> getRequiredSkillIds() {
        return requiredSkillIds;
    }

    public void setRequiredSkillIds(final List<Integer> requiredSkillIds) {
        this.requiredSkillIds = requiredSkillIds == null ? new ArrayList<>() : new ArrayList<>(requiredSkillIds);
    }
}
