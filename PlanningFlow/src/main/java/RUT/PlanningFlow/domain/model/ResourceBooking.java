package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.BookingStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.vo.DateTimeRange;
import RUT.PlanningFlow.domain.utils.DomainAssert;

import java.time.LocalDateTime;

public class ResourceBooking {
    private final Integer id;
    private final Task task;
    private final Resource resource;
    private BookingStatus status;
    private DateTimeRange reservationWindow;

    public ResourceBooking(
            final Integer id,
            final Task task,
            final Resource resource,
            final BookingStatus status,
            final LocalDateTime reservedFrom,
            final LocalDateTime reservedTo
    ) {
        this.id = id;
        DomainAssert.notNull(task, "Задача бронирования обязательна", "TASK_REQUIRED");
        DomainAssert.notNull(resource, "Ресурс бронирования обязателен", "RESOURCE_REQUIRED");
        DomainAssert.notNull(status, "Статус бронирования обязателен", "BOOKING_STATUS_REQUIRED");
        this.task = task;
        this.resource = resource;
        this.status = status;
        this.reservationWindow = new DateTimeRange(reservedFrom, reservedTo);
        validateReservationWindow();
    }

    public void confirm() {
        if (status == BookingStatus.CONFIRMED) {
            return;
        }
        if (status != BookingStatus.REQUESTED) {
            throw new DomainException("Подтвердить можно только бронь в статусе REQUESTED", "INVALID_BOOKING_STATE");
        }
        this.status = BookingStatus.CONFIRMED;
    }

    public void fail() {
        if (status == BookingStatus.FAILED) {
            return;
        }
        if (status == BookingStatus.CONFIRMED) {
            throw new DomainException("Подтвержденную бронь нельзя пометить как FAILED", "INVALID_BOOKING_STATE");
        }
        this.status = BookingStatus.FAILED;
    }

    public void cancel() {
        if (status == BookingStatus.CANCELLED) {
            return;
        }
        this.status = BookingStatus.CANCELLED;
    }

    public void reschedule(final LocalDateTime newReservedFrom, final LocalDateTime newReservedTo) {
        final DateTimeRange newWindow = new DateTimeRange(newReservedFrom, newReservedTo);
        if (status == BookingStatus.CANCELLED || status == BookingStatus.FAILED) {
            throw new DomainException("Нельзя перенести неактивную бронь", "INVALID_BOOKING_STATE");
        }
        this.reservationWindow = newWindow;
        validateReservationWindow();
    }

    private void validateReservationWindow() {
        final LocalDateTime from = reservationWindow.getStart();
        final LocalDateTime to = reservationWindow.getEnd();

        final Event event = task.getEvent();
        if (event != null) {
            final LocalDateTime eventStart = event.getStartDate();
            final LocalDateTime eventEnd = event.getEndDate();
            if (eventStart != null && eventEnd != null) {
                if (from.isBefore(eventStart) || to.isAfter(eventEnd)) {
                    throw new DomainException(
                            "Окно бронирования должно быть внутри временных рамок мероприятия",
                            "BOOKING_OUT_OF_EVENT_WINDOW"
                    );
                }
            }
        }
    }

    public Integer getId() { return id; }
    public Task getTask() { return task; }
    public Resource getResource() { return resource; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getReservedFrom() { return reservationWindow.getStart(); }
    public LocalDateTime getReservedTo() { return reservationWindow.getEnd(); }
    public DateTimeRange getReservationWindow() { return reservationWindow; }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResourceBooking that = (ResourceBooking) o;
        return id != null && that.id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}