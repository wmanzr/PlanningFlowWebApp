package RUT.PlanningFlow.application.port.out.repository;

import RUT.PlanningFlow.application.dto.event.EventPostMortemAiReportResponseDto;

import java.util.Optional;

public interface EventAiPostmortemReportRepositoryPort {

    void upsertPending(Integer eventId);

    void updateCompleted(Integer eventId, String reportText);

    void updateFailed(Integer eventId, String errorMessage);

    Optional<EventPostMortemAiReportResponseDto> findResponseByEventId(Integer eventId);
}
