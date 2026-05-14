package RUT.PlanningFlow.application.service.booking;

import RUT.PlanningFlow.application.dto.resource.ResourceBookingResponseDto;
import RUT.PlanningFlow.application.port.in.booking.GetResourceBookingDetailsQuery;
import RUT.PlanningFlow.application.port.out.repository.ResourceBookingRepositoryPort;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GetResourceBookingDetailsService implements GetResourceBookingDetailsQuery {

    private final ResourceBookingRepositoryPort bookingRepository;

    public GetResourceBookingDetailsService(final ResourceBookingRepositoryPort bookingRepository) {
        DomainAssert.notNull(bookingRepository, "Репозиторий бронирований обязателен", "RESOURCE_BOOKING_REPOSITORY_REQUIRED");
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Optional<ResourceBookingResponseDto> execute(final Integer bookingId) {
        DomainAssert.notNull(bookingId, "ID бронирования обязателен", "BOOKING_ID_REQUIRED");
        return bookingRepository.findById(bookingId).map(ResourceBookingResponseDto::from);
    }
}