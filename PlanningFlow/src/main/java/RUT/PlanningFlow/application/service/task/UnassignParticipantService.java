package RUT.PlanningFlow.application.service.task;

import RUT.PlanningFlow.application.port.in.task.UnassignParticipantUseCase;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.service.notification.NotificationService;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
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
public class UnassignParticipantService implements UnassignParticipantUseCase {

    private final TaskRepositoryPort taskRepository;
    private final AssignmentRepositoryPort assignmentRepository;
    private final UserRepositoryPort userRepository;
    private final NotificationService notificationService;

    public UnassignParticipantService(
            final TaskRepositoryPort taskRepository,
            final AssignmentRepositoryPort assignmentRepository,
            final UserRepositoryPort userRepository,
            final NotificationService notificationService
    ) {
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(assignmentRepository, "Репозиторий назначений обязателен", "ASSIGNMENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(notificationService, "Уведомления обязательны", "NOTIFICATION_SERVICE_REQUIRED");
        this.taskRepository = taskRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void execute(final Integer callerUserId, final Integer taskId, final Integer userId) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(taskId, "ID задачи обязателен", "TASK_ID_REQUIRED");
        DomainAssert.notNull(userId, "ID участника обязателен", "USER_ID_REQUIRED");

        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DomainException("Задача не найдена", "TASK_NOT_FOUND"));
        PlanningAccessPolicy.assertCanManageTaskAsPlanner(actor, task);

        final Assignment assignment = assignmentRepository.findActiveForTaskAndUser(taskId, userId)
                .orElseThrow(() -> new DomainException("Активное назначение не найдено", "ASSIGNMENT_NOT_FOUND"));

        assignment.cancelByCoordinator(LocalDateTime.now());
        task.unassign(assignment);

        assignmentRepository.update(assignment);
        taskRepository.update(task);

        final Integer assignmentId = assignment.getId();
        final User participant = userRepository.findById(userId).orElse(null);
        if (participant != null && assignmentId != null) {
            notificationService.notifyParticipantAssignmentRemoved(participant, task, assignmentId);
        }
    }
}
