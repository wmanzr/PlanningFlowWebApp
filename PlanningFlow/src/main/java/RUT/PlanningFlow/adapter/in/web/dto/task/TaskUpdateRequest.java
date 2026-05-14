package RUT.PlanningFlow.adapter.in.web.dto.task;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class TaskUpdateRequest {

    @Size(max = 2048, message = "Название слишком длинное")
    private String newTitle;

    private LocalDateTime newStartTime;

    private LocalDateTime newEndTime;

    @DecimalMin(value = "-90.0", inclusive = true, message = "Широта должна быть не меньше -90")
    @DecimalMax(value = "90.0", inclusive = true, message = "Широта должна быть не больше 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", inclusive = true, message = "Долгота должна быть не меньше -180")
    @DecimalMax(value = "180.0", inclusive = true, message = "Долгота должна быть не больше 180")
    private Double longitude;

    private List<@Positive(message = "ID навыка должен быть положительным") Integer> requiredSkillIds;

    private List<@Positive(message = "ID зависимости должен быть положительным") Integer> dependencyIds;

    public TaskUpdateRequest() {
    }

    @AssertTrue(message = "Даты начала и окончания задаются вместе, интервал должен быть корректным")
    boolean isScheduleConsistent() {
        if (newStartTime == null && newEndTime == null) {
            return true;
        }
        if (newStartTime == null || newEndTime == null) {
            return false;
        }
        return !newStartTime.isAfter(newEndTime);
    }

    @AssertTrue(message = "Задача не может длиться больше 8 часов")
    boolean isNewScheduleWithinMaxDuration() {
        if (newStartTime == null || newEndTime == null) {
            return true;
        }
        final Duration duration = Duration.between(newStartTime, newEndTime);
        return !duration.minusHours(8).isPositive();
    }

    @AssertTrue(message = "Широта и долгота задаются вместе")
    boolean isGeoPairConsistent() {
        if (latitude == null && longitude == null) {
            return true;
        }
        return latitude != null && longitude != null;
    }

    public String getNewTitle() {
        return newTitle;
    }

    public void setNewTitle(final String newTitle) {
        this.newTitle = newTitle;
    }

    public LocalDateTime getNewStartTime() {
        return newStartTime;
    }

    public void setNewStartTime(final LocalDateTime newStartTime) {
        this.newStartTime = newStartTime;
    }

    public LocalDateTime getNewEndTime() {
        return newEndTime;
    }

    public void setNewEndTime(final LocalDateTime newEndTime) {
        this.newEndTime = newEndTime;
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
        this.requiredSkillIds = requiredSkillIds;
    }

    public List<Integer> getDependencyIds() {
        return dependencyIds;
    }

    public void setDependencyIds(final List<Integer> dependencyIds) {
        this.dependencyIds = dependencyIds;
    }
}
