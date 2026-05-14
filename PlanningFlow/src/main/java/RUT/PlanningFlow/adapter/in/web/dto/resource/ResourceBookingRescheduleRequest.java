package RUT.PlanningFlow.adapter.in.web.dto.resource;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ResourceBookingRescheduleRequest {

    @NotNull(message = "Начало интервала обязательно")
    private LocalDateTime reservedFrom;

    @NotNull(message = "Окончание интервала обязательно")
    private LocalDateTime reservedTo;

    public ResourceBookingRescheduleRequest() {
    }

    @AssertTrue(message = "Интервал некорректен: начало не может быть позже окончания")
    boolean isValidRange() {
        if (reservedFrom == null || reservedTo == null) {
            return true;
        }
        return !reservedFrom.isAfter(reservedTo);
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
