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
    private final ResourceBookingResponseDtoMapper resourceBookingResponseDtoMapper;

    public GetResourceBookingDetailsService(
            final ResourceBookingRepositoryPort bookingRepository,
            final ResourceBookingResponseDtoMapper resourceBookingResponseDtoMapper
    ) {
        DomainAssert.notNull(bookingRepository, "Репозиторий бронирований обязателен", "RESOURCE_BOOKING_REPOSITORY_REQUIRED");
        DomainAssert.notNull(resourceBookingResponseDtoMapper, "Маппер ответа по бронированию обязателен", "RESOURCE_BOOKING_RESPONSE_DTO_MAPPER_REQUIRED");
        this.bookingRepository = bookingRepository;
        this.resourceBookingResponseDtoMapper = resourceBookingResponseDtoMapper;
    }

    @Override
    public Optional<ResourceBookingResponseDto> execute(final Integer bookingId) {
        DomainAssert.notNull(bookingId, "ID бронирования обязателен", "BOOKING_ID_REQUIRED");
        return bookingRepository.findById(bookingId).map(resourceBookingResponseDtoMapper::toResponse);
    }
}