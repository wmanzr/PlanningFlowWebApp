package RUT.PlanningFlow.application.service.task;

import RUT.PlanningFlow.application.port.in.task.AssignParticipantUseCase;
import RUT.PlanningFlow.application.service.notification.NotificationService;
import RUT.PlanningFlow.application.service.scheduling.ParticipantScheduleGuard;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Assignment;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@Transactional
public class AssignParticipantService implements AssignParticipantUseCase {

    private final TaskRepositoryPort taskRepository;
    private final UserRepositoryPort userRepository;
    private final AssignmentRepositoryPort assignmentRepository;
    private final NotificationService realtimeNotifications;
    private final ParticipantScheduleGuard scheduleGuard;

    public AssignParticipantService(
            final TaskRepositoryPort taskRepository,
            final UserRepositoryPort userRepository,
            final AssignmentRepositoryPort assignmentRepository,
            final NotificationService realtimeNotifications,
            final ParticipantScheduleGuard scheduleGuard
    ) {
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(assignmentRepository, "Репозиторий назначений обязателен", "ASSIGNMENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(realtimeNotifications, "Уведомления обязательны", "REALTIME_NOTIFICATIONS_REQUIRED");
        DomainAssert.notNull(scheduleGuard, "Проверка расписания обязательна", "SCHEDULE_GUARD_REQUIRED");
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.realtimeNotifications = realtimeNotifications;
        this.scheduleGuard = scheduleGuard;
    }

    @Override
    public Integer execute(final Integer callerUserId, final Integer taskId, final Integer userId) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(taskId, "ID задачи обязателен", "TASK_ID_REQUIRED");
        DomainAssert.notNull(userId, "ID участника обязателен", "USER_ID_REQUIRED");

        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DomainException("Задача не найдена", "TASK_NOT_FOUND"));
        PlanningAccessPolicy.assertCanManageTaskAsPlanner(actor, task);
        if (task.getStatus() == TaskStatus.DONE || task.getStatus() == TaskStatus.CANCELLED) {
            throw new DomainException(
                    "Нельзя назначить участника на завершенную или отмененную задачу",
                    "TASK_CLOSED_FOR_ASSIGNMENT"
            );
        }

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("Участник не найден", "USER_NOT_FOUND"));

        scheduleGuard.assertNotAlreadyAssignedOnTask(taskId, userId);
        scheduleGuard.assertNoScheduleConflict(
                userId,
                task.getStartTime(),
                task.getEndTime(),
                taskId,
                ParticipantScheduleGuard.defaultMinTechnicalGap()
        );

        final Assignment assignment = new Assignment(
                null,
                task,
                user,
                AssignStatus.PENDING,
                LocalDateTime.now(),
                null,
                null
        );

        final int assignmentId = assignmentRepository.create(assignment)
                .orElseThrow(() -> new DomainException("Не удалось создать назначение", "ASSIGNMENT_CREATE_FAILED"));

        realtimeNotifications.notifyParticipantAssignmentCreated(user, task, assignmentId);

        return assignmentId;
    }
}
