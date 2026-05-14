package RUT.PlanningFlow.application.service.event;

import RUT.PlanningFlow.application.dto.event.EventPostMortemAiReportResponseDto;
import RUT.PlanningFlow.application.port.in.event.GetEventPostMortemAiReportQuery;
import RUT.PlanningFlow.application.port.out.repository.EventAiPostmortemReportRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GetEventPostMortemAiReportService implements GetEventPostMortemAiReportQuery {

    private final EventRepositoryPort eventRepository;
    private final UserRepositoryPort userRepository;
    private final EventAiPostmortemReportRepositoryPort reportRepository;

    public GetEventPostMortemAiReportService(
            final EventRepositoryPort eventRepository,
            final UserRepositoryPort userRepository,
            final EventAiPostmortemReportRepositoryPort reportRepository
    ) {
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(reportRepository, "Репозиторий отчетов ИИ обязателен", "EVENT_AI_REPORT_REPOSITORY_REQUIRED");
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
    }

    @Override
    public Optional<EventPostMortemAiReportResponseDto> execute(final Integer callerUserId, final Integer eventId) {
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        PlanningAccessPolicy.assertCanManageEvent(actor, event);
        if (event.getStatus() != EventStatus.COMPLETED) {
            return Optional.empty();
        }
        return reportRepository.findResponseByEventId(eventId)
                .or(() -> Optional.of(new EventPostMortemAiReportResponseDto("PENDING", null, null, null)));
    }
}
