package RUT.PlanningFlow.application.service.user;

import RUT.PlanningFlow.application.port.in.user.AcceptAssignmentUseCase;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.service.notification.NotificationService;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Assignment;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AcceptAssignmentService implements AcceptAssignmentUseCase {

    private final AssignmentRepositoryPort assignmentRepository;
    private final TaskRepositoryPort taskRepository;
    private final NotificationService realtimeNotifications;

    public AcceptAssignmentService(
            final AssignmentRepositoryPort assignmentRepository,
            final TaskRepositoryPort taskRepository,
            final NotificationService realtimeNotifications
    ) {
        DomainAssert.notNull(assignmentRepository, "Репозиторий назначений обязателен", "ASSIGNMENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(realtimeNotifications, "Уведомления обязательны", "REALTIME_NOTIFICATIONS_REQUIRED");
        this.assignmentRepository = assignmentRepository;
        this.taskRepository = taskRepository;
        this.realtimeNotifications = realtimeNotifications;
    }

    @Override
    public void execute(final Integer assignmentId) {
        DomainAssert.notNull(assignmentId, "ID назначения обязателен", "ASSIGNMENT_ID_REQUIRED");
        final Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new DomainException("Назначение не найдено", "ASSIGNMENT_NOT_FOUND"));
        assignment.accept(LocalDateTime.now());
        assignmentRepository.update(assignment);
        final Task task = assignment.getTask();
        task.assign(assignment);
        taskRepository.update(task);
        realtimeNotifications.notifyCoordinatorsAssignmentAccepted(assignment, assignment.getUser());
    }
}