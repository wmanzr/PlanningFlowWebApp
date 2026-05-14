package RUT.PlanningFlow.application.port.in.booking;

import java.util.Optional;

public interface ManageResourceBookingStatusUseCase {
    Optional<Integer> confirm(Integer bookingId);
    Optional<Integer> fail(Integer bookingId);
    Optional<Integer> cancel(Integer bookingId);
}