package RUT.PlanningFlow.application.service.event;

import RUT.PlanningFlow.application.dto.event.EventDashboardResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.event.GetEventDashboardQuery;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.IncidentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Assignment;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Incident;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class GetEventDashboardService implements GetEventDashboardQuery {

    private final EventRepositoryPort eventRepository;
    private final TaskRepositoryPort taskRepository;
    private final UserRepositoryPort userRepository;
    private final AssignmentRepositoryPort assignmentRepository;
    private final IncidentRepositoryPort incidentRepository;

    public GetEventDashboardService(
            final EventRepositoryPort eventRepository,
            final TaskRepositoryPort taskRepository,
            final UserRepositoryPort userRepository,
            final AssignmentRepositoryPort assignmentRepository,
            final IncidentRepositoryPort incidentRepository
    ) {
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(assignmentRepository, "Репозиторий назначений обязателен", "ASSIGNMENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(incidentRepository, "Репозиторий инцидентов обязателен", "INCIDENT_REPOSITORY_REQUIRED");
        this.eventRepository = eventRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.incidentRepository = incidentRepository;
    }

    @Override
    public EventDashboardResponseDto execute(final Integer callerUserId, final Integer eventId) {
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");

        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new DomainException("Мероприятие не найдено", "EVENT_NOT_FOUND"));
        final boolean assigned = assignmentRepository.existsAssignmentForUserOnEvent(actor.getId(), eventId);
        if (!PlanningAccessPolicy.canViewEvent(actor, event, assigned)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final List<Task> tasks = taskRepository.findTasksForEvent(eventId);
        final double progress = event.calculateProgress(tasks);

        int cancelledTasks = 0;
        int activeTasks = 0;
        int completedTasks = 0;
        for (final Task t : tasks) {
            if (t == null) {
                continue;
            }
            final TaskStatus st = t.getStatus();
            if (st == TaskStatus.CANCELLED) {
                cancelledTasks++;
            } else if (st == TaskStatus.DONE) {
                completedTasks++;
            } else {
                activeTasks++;
            }
        }

        final Set<Integer> executorUserIds = new HashSet<>();
        for (final Task t : tasks) {
            if (t == null || t.getId() == null) {
                continue;
            }
            for (final Assignment a : assignmentRepository.findByTaskId(t.getId())) {
                if (a == null || a.getStatus() != AssignStatus.ACCEPTED) {
                    continue;
                }
                final User u = a.getUser();
                if (u != null && u.getId() != null) {
                    executorUserIds.add(u.getId());
                }
            }
        }

        final PageResult<Incident> incidentPage =
                incidentRepository.findByEventId(eventId, new PageQuery(1, 1));

        return new EventDashboardResponseDto(
                event.getId(),
                event.getTitle(),
                event.getStartDate(),
                event.getEndDate(),
                event.getStatus(),
                tasks.size(),
                activeTasks,
                completedTasks,
                progress,
                executorUserIds.size(),
                cancelledTasks,
                Math.toIntExact(incidentPage.totalElements())
        );
    }
}
