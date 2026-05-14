package RUT.PlanningFlow.application.service.task;

import RUT.PlanningFlow.application.port.in.task.ManageTaskStatusUseCase;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.ResourceBookingRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.model.Assignment;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.ResourceBooking;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class ManageTaskStatusService implements ManageTaskStatusUseCase {

    private final TaskRepositoryPort taskRepository;
    private final EventRepositoryPort eventRepository;
    private final ResourceBookingRepositoryPort resourceBookingRepository;
    private final AssignmentRepositoryPort assignmentRepository;
    private final UserRepositoryPort userRepository;

    public ManageTaskStatusService(
            final TaskRepositoryPort taskRepository,
            final EventRepositoryPort eventRepository,
            final ResourceBookingRepositoryPort resourceBookingRepository,
            final AssignmentRepositoryPort assignmentRepository,
            final UserRepositoryPort userRepository
    ) {
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(resourceBookingRepository, "Репозиторий бронирований обязателен", "RESOURCE_BOOKING_REPOSITORY_REQUIRED");
        DomainAssert.notNull(assignmentRepository, "Репозиторий назначений обязателен", "ASSIGNMENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        this.taskRepository = taskRepository;
        this.eventRepository = eventRepository;
        this.resourceBookingRepository = resourceBookingRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Integer> startExecution(final Integer callerUserId, final Integer taskId) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(taskId, "ID задачи обязателен", "TASK_ID_REQUIRED");
        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Optional<Task> exTask = taskRepository.findById(taskId);
        if (exTask.isEmpty()) {
            return Optional.empty();
        }
        final Task task = exTask.get();
        final boolean assignedToTask = assignmentRepository.existsAssignmentForUserOnTask(callerUserId, taskId);
        PlanningAccessPolicy.assertCanViewTask(actor, task, assignedToTask);
        final Integer eventId = task.getEvent() == null ? null : task.getEvent().getId();
        final long inProgressBefore = eventId == null
                ? 0L
                : taskRepository.countByEventIdAndStatus(eventId, TaskStatus.IN_PROGRESS);
        task.startExecution();
        final Optional<Integer> updated = taskRepository.update(task);
        if (updated.isPresent() && eventId != null) {
            maybeActivateEventWhenFirstTaskStarts(eventId, inProgressBefore);
        }
        return updated;
    }

    private void maybeActivateEventWhenFirstTaskStarts(final Integer eventId, final long inProgressCountBeforeThisTask) {
        if (inProgressCountBeforeThisTask > 0) {
            return;
        }
        final Optional<Event> loaded = eventRepository.findById(eventId);
        if (loaded.isEmpty()) {
            return;
        }
        final Event event = loaded.get();
        if (event.getStatus() != EventStatus.PLANNING) {
            return;
        }
        event.activate();
        eventRepository.update(event);
    }

    @Override
    public Optional<Integer> markAsDone(final Integer callerUserId, final Integer taskId) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(taskId, "ID задачи обязателен", "TASK_ID_REQUIRED");
        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Optional<Task> exTask = taskRepository.findById(taskId);
        if (exTask.isEmpty()) {
            return Optional.empty();
        }
        final Task task = exTask.get();
        final boolean assignedToTask = assignmentRepository.existsAssignmentForUserOnTask(callerUserId, taskId);
        PlanningAccessPolicy.assertCanViewTask(actor, task, assignedToTask);
        task.markAsDone();
        return taskRepository.update(task);
    }

    @Override
    public Optional<Integer> cancel(final Integer callerUserId, final Integer taskId) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(taskId, "ID задачи обязателен", "TASK_ID_REQUIRED");
        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Optional<Task> exTask = taskRepository.findById(taskId);
        if (exTask.isEmpty()) {
            return Optional.empty();
        }
        final Task task = exTask.get();
        PlanningAccessPolicy.assertCanManageTaskAsPlanner(actor, task);

        final LocalDateTime now = LocalDateTime.now();

        for (final ResourceBooking booking : resourceBookingRepository.findActiveForTask(taskId)) {
            booking.cancel();
            resourceBookingRepository.update(booking);
        }

        for (final Assignment assignment : assignmentRepository.findByTaskId(taskId)) {
            if (assignment.getStatus() == AssignStatus.REJECTED || assignment.getStatus() == AssignStatus.CANCELLED) {
                continue;
            }
            assignment.cancelByCoordinator(now);
            assignmentRepository.update(assignment);
        }

        task.cancel();
        return taskRepository.update(task);
    }
}