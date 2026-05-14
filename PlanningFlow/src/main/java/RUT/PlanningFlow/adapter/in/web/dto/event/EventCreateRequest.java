package RUT.PlanningFlow.adapter.in.web.dto.event;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class EventCreateRequest {

    @NotBlank(message = "Название мероприятия обязательно")
    @Size(max = 2048, message = "Название слишком длинное")
    private String title;

    @Size(max = 65535, message = "Описание слишком длинное")
    private String description;

    @NotNull(message = "Дата начала обязательна")
    private LocalDateTime startDate;

    @NotNull(message = "Дата окончания обязательна")
    private LocalDateTime endDate;

    @DecimalMin(value = "-90.0", inclusive = true, message = "Широта должна быть не меньше -90")
    @DecimalMax(value = "90.0", inclusive = true, message = "Широта должна быть не больше 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", inclusive = true, message = "Долгота должна быть не меньше -180")
    @DecimalMax(value = "180.0", inclusive = true, message = "Долгота должна быть не больше 180")
    private Double longitude;

    public EventCreateRequest() {
    }

    @AssertTrue(message = "Некорректный временной интервал: начало не может быть позже окончания")
    boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !startDate.isAfter(endDate);
    }

    @AssertTrue(message = "Широта и долгота задаются вместе")
    boolean isGeoPairConsistent() {
        if (latitude == null && longitude == null) {
            return true;
        }
        return latitude != null && longitude != null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(final LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDateTime endDate) {
        this.endDate = endDate;
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
}
