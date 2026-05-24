package RUT.PlanningFlow.application.port.in.booking;

import RUT.PlanningFlow.application.dto.resource.ResourceBookingResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.domain.enums.BookingStatus;
import RUT.PlanningFlow.domain.enums.ResourceType;

import java.util.List;
import java.util.Optional;

public interface ListResourceBookingsForEventQuery {

    PageResult<ResourceBookingResponseDto> execute(
            Integer eventId,
            PageQuery pageQuery,
            Optional<String> nameContains,
            List<BookingStatus> statuses,
            Optional<ResourceType> resourceType
    );
}
