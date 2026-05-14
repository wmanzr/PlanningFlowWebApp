package RUT.PlanningFlow.application.service.event;

import RUT.PlanningFlow.application.port.in.event.GeneratePostMortemAIReportUseCase;
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

@Service
@Transactional
public class GeneratePostMortemAIReportService implements GeneratePostMortemAIReportUseCase {

    private final EventRepositoryPort eventRepository;
    private final UserRepositoryPort userRepository;
    private final EventAiPostmortemReportRepositoryPort eventAiPostmortemReportRepository;
    private final EventPostMortemAiGenerationTrigger postMortemAiGenerationTrigger;

    public GeneratePostMortemAIReportService(
            final EventRepositoryPort eventRepository,
            final UserRepositoryPort userRepository,
            final EventAiPostmortemReportRepositoryPort eventAiPostmortemReportRepository,
            final EventPostMortemAiGenerationTrigger postMortemAiGenerationTrigger
    ) {
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(eventAiPostmortemReportRepository, "Репозиторий отчетов ИИ обязателен", "EVENT_AI_REPORT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(postMortemAiGenerationTrigger, "Триггер отчета ИИ обязателен", "POST_MORTEM_AI_TRIGGER_REQUIRED");
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventAiPostmortemReportRepository = eventAiPostmortemReportRepository;
        this.postMortemAiGenerationTrigger = postMortemAiGenerationTrigger;
    }

    @Override
    public void execute(final Integer callerUserId, final Integer eventId) {
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        PlanningAccessPolicy.assertCanManageEvent(actor, event);
        if (event.getStatus() != EventStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Отчет ИИ доступен только для завершенного мероприятия");
        }
        eventAiPostmortemReportRepository.upsertPending(eventId);
        postMortemAiGenerationTrigger.scheduleAfterCommit(eventId);
    }
}
