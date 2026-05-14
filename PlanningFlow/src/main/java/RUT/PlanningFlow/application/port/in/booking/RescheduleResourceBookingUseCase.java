package RUT.PlanningFlow.application.port.in.booking;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RescheduleResourceBookingUseCase {
    Optional<Integer> execute(Integer bookingId, LocalDateTime reservedFrom, LocalDateTime reservedTo);
}