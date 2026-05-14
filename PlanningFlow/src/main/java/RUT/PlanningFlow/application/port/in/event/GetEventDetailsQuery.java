package RUT.PlanningFlow.application.port.in.event;

import RUT.PlanningFlow.application.dto.event.EventResponseDto;

import java.util.Optional;

public interface GetEventDetailsQuery {
    Optional<EventResponseDto> execute(Integer callerUserId, Integer eventId);
}