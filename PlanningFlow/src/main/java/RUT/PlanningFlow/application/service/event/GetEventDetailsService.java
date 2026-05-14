package RUT.PlanningFlow.application.service.event;

import RUT.PlanningFlow.application.dto.event.EventResponseDto;
import RUT.PlanningFlow.application.port.in.event.GetEventDetailsQuery;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
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
public class GetEventDetailsService implements GetEventDetailsQuery {

    private final EventRepositoryPort eventRepository;
    private final UserRepositoryPort userRepository;
    private final AssignmentRepositoryPort assignmentRepository;
    private final TaskRepositoryPort taskRepository;

    public GetEventDetailsService(
            final EventRepositoryPort eventRepository,
            final UserRepositoryPort userRepository,
            final AssignmentRepositoryPort assignmentRepository,
            final TaskRepositoryPort taskRepository
    ) {
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(assignmentRepository, "Репозиторий назначений обязателен", "ASSIGNMENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public Optional<EventResponseDto> execute(final Integer callerUserId, final Integer eventId) {
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");

        final Optional<Event> exEvent = eventRepository.findById(eventId);
        if (exEvent.isEmpty()) {
            return Optional.empty();
        }
        final Event event = exEvent.get();
        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final boolean assigned = assignmentRepository.existsAssignmentForUserOnEvent(actor.getId(), eventId);
        if (!PlanningAccessPolicy.canViewEvent(actor, event, assigned)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        final long tasks = taskRepository.countTasksForEvent(eventId);
        return Optional.of(EventResponseDto.from(event, tasks));
    }
}
