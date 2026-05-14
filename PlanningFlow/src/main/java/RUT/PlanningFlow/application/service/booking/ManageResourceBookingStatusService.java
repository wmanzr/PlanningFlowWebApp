package RUT.PlanningFlow.application.service.booking;

import RUT.PlanningFlow.application.port.in.booking.ManageResourceBookingStatusUseCase;
import RUT.PlanningFlow.application.port.out.repository.ResourceBookingRepositoryPort;
import RUT.PlanningFlow.domain.model.ResourceBooking;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ManageResourceBookingStatusService implements ManageResourceBookingStatusUseCase {

    private final ResourceBookingRepositoryPort bookingRepository;

    public ManageResourceBookingStatusService(final ResourceBookingRepositoryPort bookingRepository) {
        DomainAssert.notNull(bookingRepository, "Репозиторий бронирований обязателен", "RESOURCE_BOOKING_REPOSITORY_REQUIRED");
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Optional<Integer> confirm(final Integer bookingId) {
        DomainAssert.notNull(bookingId, "ID бронирования обязателен", "BOOKING_ID_REQUIRED");
        final Optional<ResourceBooking> ex = bookingRepository.findById(bookingId);
        if (ex.isEmpty()) {
            return Optional.empty();
        }
        final ResourceBooking booking = ex.get();
        booking.confirm();
        return bookingRepository.update(booking);
    }

    @Override
    public Optional<Integer> fail(final Integer bookingId) {
        DomainAssert.notNull(bookingId, "ID бронирования обязателен", "BOOKING_ID_REQUIRED");
        final Optional<ResourceBooking> ex = bookingRepository.findById(bookingId);
        if (ex.isEmpty()) {
            return Optional.empty();
        }
        final ResourceBooking booking = ex.get();
        booking.fail();
        return bookingRepository.update(booking);
    }

    @Override
    public Optional<Integer> cancel(final Integer bookingId) {
        DomainAssert.notNull(bookingId, "ID бронирования обязателен", "BOOKING_ID_REQUIRED");
        final Optional<ResourceBooking> ex = bookingRepository.findById(bookingId);
        if (ex.isEmpty()) {
            return Optional.empty();
        }
        final ResourceBooking booking = ex.get();
        booking.cancel();
        return bookingRepository.update(booking);
    }
}