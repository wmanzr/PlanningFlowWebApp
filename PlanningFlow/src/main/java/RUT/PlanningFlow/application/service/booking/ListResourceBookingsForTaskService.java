package RUT.PlanningFlow.application.service.booking;

import RUT.PlanningFlow.application.dto.resource.ResourceBookingResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.booking.ListResourceBookingsForTaskQuery;
import RUT.PlanningFlow.application.port.out.repository.ResourceBookingRepositoryPort;
import RUT.PlanningFlow.domain.model.ResourceBooking;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListResourceBookingsForTaskService implements ListResourceBookingsForTaskQuery {

    private final ResourceBookingRepositoryPort bookingRepository;

    public ListResourceBookingsForTaskService(final ResourceBookingRepositoryPort bookingRepository) {
        DomainAssert.notNull(bookingRepository, "Репозиторий бронирований обязателен", "RESOURCE_BOOKING_REPOSITORY_REQUIRED");
        this.bookingRepository = bookingRepository;
    }

    @Override
    public PageResult<ResourceBookingResponseDto> execute(final Integer taskId, final PageQuery pageQuery) {
        DomainAssert.notNull(taskId, "ID задачи обязателен", "TASK_ID_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");

        final PageResult<ResourceBooking> page = bookingRepository.findByTaskId(taskId, pageQuery);
        final List<ResourceBookingResponseDto> items = new ArrayList<>(page.items().size());
        for (final ResourceBooking b : page.items()) {
            items.add(ResourceBookingResponseDto.from(b));
        }
        return new PageResult<>(items, page.totalElements(), page.totalPages());
    }
}