package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.BookingStatus;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceBookingTest {

    private static final LocalDateTime T0 = DomainFixtures.EVENT_RANGE_START.plusHours(2);
    private static final LocalDateTime T1 = T0.plusHours(4);

    @Test
    void confirm_from_requested() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final InternalResource resource = new InternalResource(1, "Hall", ResourceType.EQUIPMENT, "INV-9");
        final ResourceBooking booking = new ResourceBooking(
                1,
                task,
                resource,
                BookingStatus.REQUESTED,
                T0.plusMinutes(30),
                T0.plusHours(2)
        );

        booking.confirm();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void reservation_outside_event_window_rejected() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final InternalResource resource = new InternalResource(1, "Hall", ResourceType.EQUIPMENT, "INV-9");

        assertThatThrownBy(() -> new ResourceBooking(
                1,
                task,
                resource,
                BookingStatus.REQUESTED,
                DomainFixtures.EVENT_RANGE_START.minusDays(1),
                DomainFixtures.EVENT_RANGE_START.minusDays(1).plusHours(1)
        ))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "BOOKING_OUT_OF_EVENT_WINDOW");
    }
}
