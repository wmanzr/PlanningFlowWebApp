package RUT.PlanningFlow.application.service.booking;

import RUT.PlanningFlow.application.port.in.booking.RescheduleResourceBookingUseCase;
import RUT.PlanningFlow.application.port.out.repository.ResourceBookingRepositoryPort;
import RUT.PlanningFlow.domain.model.ResourceBooking;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class RescheduleResourceBookingService implements RescheduleResourceBookingUseCase {

    private final ResourceBookingRepositoryPort bookingRepository;

    public RescheduleResourceBookingService(final ResourceBookingRepositoryPort bookingRepository) {
        DomainAssert.notNull(bookingRepository, "Репозиторий бронирований обязателен", "RESOURCE_BOOKING_REPOSITORY_REQUIRED");
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Optional<Integer> execute(
            final Integer bookingId,
            final LocalDateTime reservedFrom,
            final LocalDateTime reservedTo
    ) {
        DomainAssert.notNull(bookingId, "ID бронирования обязателен", "BOOKING_ID_REQUIRED");
        DomainAssert.notNull(reservedFrom, "Начало интервала обязательно", "BOOKING_RESERVED_FROM_REQUIRED");
        DomainAssert.notNull(reservedTo, "Окончание интервала обязательно", "BOOKING_RESERVED_TO_REQUIRED");

        final Optional<ResourceBooking> ex = bookingRepository.findById(bookingId);
        if (ex.isEmpty()) {
            return Optional.empty();
        }
        final ResourceBooking booking = ex.get();
        booking.reschedule(reservedFrom, reservedTo);
        return bookingRepository.update(booking);
    }
}