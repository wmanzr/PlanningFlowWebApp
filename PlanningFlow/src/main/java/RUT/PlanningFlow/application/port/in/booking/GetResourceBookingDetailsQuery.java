package RUT.PlanningFlow.application.port.in.booking;

import RUT.PlanningFlow.application.dto.resource.ResourceBookingResponseDto;

import java.util.Optional;

public interface GetResourceBookingDetailsQuery {
    Optional<ResourceBookingResponseDto> execute(Integer bookingId);
}