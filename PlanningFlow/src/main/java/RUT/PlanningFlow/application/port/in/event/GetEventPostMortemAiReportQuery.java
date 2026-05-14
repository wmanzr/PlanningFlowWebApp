package RUT.PlanningFlow.application.port.in.event;

import RUT.PlanningFlow.application.dto.event.EventPostMortemAiReportResponseDto;

import java.util.Optional;

public interface GetEventPostMortemAiReportQuery {
    Optional<EventPostMortemAiReportResponseDto> execute(Integer callerUserId, Integer eventId);
}
