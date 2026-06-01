package RUT.PlanningFlow.application.service.booking;

import RUT.PlanningFlow.application.dto.resource.ResourceBookingResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.booking.ListResourceBookingsForEventQuery;
import RUT.PlanningFlow.application.port.out.repository.ResourceBookingRepositoryPort;
import RUT.PlanningFlow.domain.enums.BookingStatus;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.model.ResourceBooking;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ListResourceBookingsForEventService implements ListResourceBookingsForEventQuery {

    private static final List<BookingStatus> ALL_STATUSES = List.copyOf(Arrays.asList(BookingStatus.values()));

    private final ResourceBookingRepositoryPort bookingRepository;
    private final ResourceBookingResponseDtoMapper resourceBookingResponseDtoMapper;

    public ListResourceBookingsForEventService(
            final ResourceBookingRepositoryPort bookingRepository,
            final ResourceBookingResponseDtoMapper resourceBookingResponseDtoMapper
    ) {
        DomainAssert.notNull(bookingRepository, "Репозиторий бронирований обязателен", "RESOURCE_BOOKING_REPOSITORY_REQUIRED");
        DomainAssert.notNull(resourceBookingResponseDtoMapper, "Маппер ответа по бронированию обязателен", "RESOURCE_BOOKING_RESPONSE_DTO_MAPPER_REQUIRED");
        this.bookingRepository = bookingRepository;
        this.resourceBookingResponseDtoMapper = resourceBookingResponseDtoMapper;
    }

    @Override
    public PageResult<ResourceBookingResponseDto> execute(
            final Integer eventId,
            final PageQuery pageQuery,
            final Optional<String> nameContains,
            final List<BookingStatus> statuses,
            final Optional<ResourceType> resourceType
    ) {
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");

        final List<BookingStatus> effectiveStatuses = (statuses == null || statuses.isEmpty()) ? ALL_STATUSES : List.copyOf(statuses);
        DomainAssert.isTrue(!effectiveStatuses.isEmpty(), "Нужен хотя бы один статус", "BOOKING_STATUSES_REQUIRED");

        final PageResult<ResourceBooking> page = bookingRepository.findForEvent(
                eventId,
                pageQuery,
                nameContains,
                effectiveStatuses,
                resourceType
        );
        final List<ResourceBookingResponseDto> items = resourceBookingResponseDtoMapper.toResponses(page.items());
        return new PageResult<>(items, page.totalElements(), page.totalPages());
    }
}
