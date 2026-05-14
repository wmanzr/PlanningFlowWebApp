package RUT.PlanningFlow.application.service.event;

import RUT.PlanningFlow.application.port.in.event.UpdateEventUseCase;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.application.service.notification.NotificationService;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UpdateEventService implements UpdateEventUseCase {

    private final EventRepositoryPort eventRepository;
    private final UserRepositoryPort userRepository;
    private final TaskRepositoryPort taskRepository;
    private final NotificationService notificationService;

    public UpdateEventService(
            final EventRepositoryPort eventRepository,
            final UserRepositoryPort userRepository,
            final TaskRepositoryPort taskRepository,
            final NotificationService notificationService
    ) {
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(notificationService, "Сервис уведомлений обязателен", "NOTIFICATION_SERVICE_REQUIRED");
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Optional<Integer> execute(
            final Integer callerUserId,
            final Integer eventId,
            final String newTitle,
            final String newDescription,
            final LocalDateTime newStartDate,
            final LocalDateTime newEndDate,
            final GeoPoint newLocation,
            final List<Integer> coordinatorIds
    ) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");

        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        final Optional<Event> exEvent = eventRepository.findById(eventId);
        if (exEvent.isEmpty()) {
            return Optional.empty();
        }

        final Event event = exEvent.get();
        final boolean editsDetails =
                newTitle != null
                        || newDescription != null
                        || newStartDate != null
                        || newEndDate != null
                        || newLocation != null;
        final boolean editsCoordinators = coordinatorIds != null;
        if (editsDetails) {
            PlanningAccessPolicy.assertCanEditEvent(actor, event);
        }
        if (editsCoordinators) {
            PlanningAccessPolicy.assertCanManageEvent(actor, event);
        }
        if (!editsDetails && !editsCoordinators) {
            PlanningAccessPolicy.assertCanManageEvent(actor, event);
        }
        if (newTitle != null || newDescription != null) {
            event.updateInfo(
                    newTitle != null ? newTitle : event.getTitle(),
                    newDescription != null ? newDescription : event.getDescription()
            );
        }

        if (newStartDate != null || newEndDate != null) {
            final List<Task> tasks = taskRepository.findTasksForEvent(eventId);
            final LocalDateTime start = newStartDate != null ? newStartDate : event.getStartDate();
            final LocalDateTime end = newEndDate != null ? newEndDate : event.getEndDate();
            event.updateDates(start, end, tasks);
        }

        if (newLocation != null) {
            event.updateLocation(newLocation);
        }

        if (coordinatorIds != null) {
            final Set<Integer> coordinatorIdsBefore = new HashSet<>();
            for (final User existing : event.getCoordinators()) {
                if (existing != null && existing.getId() != null) {
                    coordinatorIdsBefore.add(existing.getId());
                }
            }

            final Set<Integer> desiredIds = new HashSet<>();
            for (final Integer id : coordinatorIds) {
                DomainAssert.notNull(id, "ID координатора обязателен", "EVENT_COORDINATOR_ID_REQUIRED");
                desiredIds.add(id);
            }

            for (final User existing : List.copyOf(event.getCoordinators())) {
                if (existing == null || existing.getId() == null) {
                    continue;
                }
                if (!desiredIds.contains(existing.getId())) {
                    event.removeCoordinator(existing);
                }
            }

            for (final Integer desiredId : desiredIds) {
                final boolean alreadyHas = event.getCoordinators().stream()
                        .anyMatch(u -> u != null && desiredId.equals(u.getId()));
                if (alreadyHas) {
                    continue;
                }
                final User coordinator = userRepository.findById(desiredId)
                        .orElseThrow(() -> new DomainException("Координатор не найден", "EVENT_COORDINATOR_NOT_FOUND"));
                event.addCoordinator(coordinator);
                if (!coordinatorIdsBefore.contains(desiredId) && !Objects.equals(desiredId, callerUserId)) {
                    notificationService.notifyUserAppointedEventCoordinator(coordinator, event);
                }
            }
        }

        return eventRepository.update(event);
    }
}