package RUT.PlanningFlow.adapter.in.web.dto.resource;

import RUT.PlanningFlow.domain.enums.ResourceType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class TaskAllocateResourcesRequest {

    @NotNull(message = "Тип ресурса обязателен")
    private ResourceType resourceType;

    @NotBlank(message = "Название ресурса обязательно")
    private String resourceName;

    @NotNull(message = "Требуемое количество обязательно")
    @Min(value = 1, message = "Требуемое количество должно быть не меньше 1")
    private Integer requiredCount;

    @NotNull(message = "Начало бронирования обязательно")
    private LocalDateTime reservedFrom;

    @NotNull(message = "Окончание бронирования обязательно")
    private LocalDateTime reservedTo;

    public TaskAllocateResourcesRequest() {
    }

    @AssertTrue(message = "Интервал бронирования некорректен")
    boolean isValidWindow() {
        if (reservedFrom == null || reservedTo == null) {
            return true;
        }
        return !reservedFrom.isAfter(reservedTo);
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(final ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(final String resourceName) {
        this.resourceName = resourceName;
    }

    public Integer getRequiredCount() {
        return requiredCount;
    }

    public void setRequiredCount(final Integer requiredCount) {
        this.requiredCount = requiredCount;
    }

    public LocalDateTime getReservedFrom() {
        return reservedFrom;
    }

    public void setReservedFrom(final LocalDateTime reservedFrom) {
        this.reservedFrom = reservedFrom;
    }

    public LocalDateTime getReservedTo() {
        return reservedTo;
    }

    public void setReservedTo(final LocalDateTime reservedTo) {
        this.reservedTo = reservedTo;
    }
}
