package RUT.PlanningFlow.application.service.task;

import RUT.PlanningFlow.application.dto.task.TaskResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.task.ListTasksForEventQuery;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListTasksForEventService implements ListTasksForEventQuery {

    private final TaskRepositoryPort taskRepository;
    private final EventRepositoryPort eventRepository;
    private final UserRepositoryPort userRepository;
    private final AssignmentRepositoryPort assignmentRepository;

    public ListTasksForEventService(
            final TaskRepositoryPort taskRepository,
            final EventRepositoryPort eventRepository,
            final UserRepositoryPort userRepository,
            final AssignmentRepositoryPort assignmentRepository
    ) {
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(assignmentRepository, "Репозиторий назначений обязателен", "ASSIGNMENT_REPOSITORY_REQUIRED");
        this.taskRepository = taskRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public PageResult<TaskResponseDto> execute(
            final Integer callerUserId,
            final Integer eventId,
            final LocalDateTime start,
            final LocalDateTime end,
            final PageQuery pageQuery
    ) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");

        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final boolean assigned = assignmentRepository.existsAssignmentForUserOnEvent(actor.getId(), eventId);
        if (!PlanningAccessPolicy.canViewEvent(actor, event, assigned)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final boolean isZeroInterval = start != null && end != null && start.equals(end);
        final PageResult<Task> page = (start == null || end == null || isZeroInterval)
                ? taskRepository.findTasksForEvent(eventId, pageQuery)
                : taskRepository.findTasksForEventBetween(eventId, start, end, pageQuery);

        final List<TaskResponseDto> items = new ArrayList<>(page.items().size());
        for (final Task t : page.items()) {
            items.add(TaskResponseDto.from(t));
        }
        return new PageResult<>(items, page.totalElements(), page.totalPages());
    }
}
