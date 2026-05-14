package RUT.PlanningFlow.application.service.event;

import RUT.PlanningFlow.application.port.in.event.ManageEventStatusUseCase;
import RUT.PlanningFlow.application.port.in.task.ManageTaskStatusUseCase;
import RUT.PlanningFlow.application.port.out.repository.EventAiPostmortemReportRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.IncidentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Incident;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ManageEventStatusService implements ManageEventStatusUseCase {

    private final EventRepositoryPort eventRepository;
    private final TaskRepositoryPort taskRepository;
    private final IncidentRepositoryPort incidentRepository;
    private final ManageTaskStatusUseCase manageTaskStatusUseCase;
    private final UserRepositoryPort userRepository;
    private final EventAiPostmortemReportRepositoryPort eventAiPostmortemReportRepository;
    private final EventPostMortemAiGenerationTrigger postMortemAiGenerationTrigger;

    public ManageEventStatusService(
            final EventRepositoryPort eventRepository,
            final TaskRepositoryPort taskRepository,
            final IncidentRepositoryPort incidentRepository,
            final ManageTaskStatusUseCase manageTaskStatusUseCase,
            final UserRepositoryPort userRepository,
            final EventAiPostmortemReportRepositoryPort eventAiPostmortemReportRepository,
            final EventPostMortemAiGenerationTrigger postMortemAiGenerationTrigger
    ) {
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(incidentRepository, "Репозиторий инцидентов обязателен", "INCIDENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(manageTaskStatusUseCase, "Управление статусом задачи обязательно", "MANAGE_TASK_STATUS_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(eventAiPostmortemReportRepository, "Репозиторий отчетов ИИ обязателен", "EVENT_AI_REPORT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(postMortemAiGenerationTrigger, "Триггер отчета ИИ обязателен", "POST_MORTEM_AI_TRIGGER_REQUIRED");
        this.eventRepository = eventRepository;
        this.taskRepository = taskRepository;
        this.incidentRepository = incidentRepository;
        this.manageTaskStatusUseCase = manageTaskStatusUseCase;
        this.userRepository = userRepository;
        this.eventAiPostmortemReportRepository = eventAiPostmortemReportRepository;
        this.postMortemAiGenerationTrigger = postMortemAiGenerationTrigger;
    }

    @Override
    public Optional<Integer> startPlanning(final Integer callerUserId, final Integer eventId) {
        final Event event = requireEventForMutation(callerUserId, eventId);
        event.startPlanning();
        return eventRepository.update(event);
    }

    @Override
    public Optional<Integer> activate(final Integer callerUserId, final Integer eventId) {
        final Event event = requireEventForMutation(callerUserId, eventId);
        event.activate();
        return eventRepository.update(event);
    }

    @Override
    public Optional<Integer> complete(final Integer callerUserId, final Integer eventId) {
        final Event event = requireEventForMutation(callerUserId, eventId);
        final List<Task> tasks = taskRepository.findTasksForEvent(eventId);
        final List<Incident> openIncidents = incidentRepository.findOpenOrInProgressByEventId(eventId);
        event.complete(tasks, openIncidents);
        final Optional<Integer> updated = eventRepository.update(event);
        for (final Incident inc : openIncidents) {
            if (inc.getStatus() == IncidentStatus.RESOLVED) {
                incidentRepository.update(inc);
            }
        }
        if (updated.isPresent()) {
            eventAiPostmortemReportRepository.upsertPending(eventId);
            postMortemAiGenerationTrigger.scheduleAfterCommit(eventId);
        }
        return updated;
    }

    @Override
    public Optional<Integer> cancel(final Integer callerUserId, final Integer eventId, final String reason) {
        final Event event = requireEventForOrganizerMutation(callerUserId, eventId);
        final List<Task> tasks = taskRepository.findTasksForEvent(eventId);
        for (final Task t : tasks) {
            manageTaskStatusUseCase.cancel(callerUserId, t.getId());
        }
        final List<Incident> openIncidents = incidentRepository.findOpenOrInProgressByEventId(eventId);
        event.cancel(reason, openIncidents);
        final Optional<Integer> updated = eventRepository.update(event);
        for (final Incident inc : openIncidents) {
            if (inc.getStatus() == IncidentStatus.RESOLVED) {
                incidentRepository.update(inc);
            }
        }
        return updated;
    }

    private Event requireEventForMutation(final Integer callerUserId, final Integer eventId) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Optional<Event> ex = eventRepository.findById(eventId);
        if (ex.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        final Event event = ex.get();
        PlanningAccessPolicy.assertCanManageEvent(actor, event);
        return event;
    }

    
    private Event requireEventForOrganizerMutation(final Integer callerUserId, final Integer eventId) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Optional<Event> ex = eventRepository.findById(eventId);
        if (ex.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        final Event event = ex.get();
        PlanningAccessPolicy.assertCanEditEvent(actor, event);
        return event;
    }
}
