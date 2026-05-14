package RUT.PlanningFlow.application.service.user;

import RUT.PlanningFlow.application.port.in.user.RejectAssignmentUseCase;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.service.notification.NotificationService;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Assignment;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class RejectAssignmentService implements RejectAssignmentUseCase {

    private final AssignmentRepositoryPort assignmentRepository;
    private final NotificationService realtimeNotifications;

    public RejectAssignmentService(
            final AssignmentRepositoryPort assignmentRepository,
            final NotificationService realtimeNotifications
    ) {
        DomainAssert.notNull(assignmentRepository, "Репозиторий назначений обязателен", "ASSIGNMENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(realtimeNotifications, "Уведомления обязательны", "REALTIME_NOTIFICATIONS_REQUIRED");
        this.assignmentRepository = assignmentRepository;
        this.realtimeNotifications = realtimeNotifications;
    }

    @Override
    public void execute(final Integer assignmentId, final String reason) {
        DomainAssert.notNull(assignmentId, "ID назначения обязателен", "ASSIGNMENT_ID_REQUIRED");
        final Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new DomainException("Назначение не найдено", "ASSIGNMENT_NOT_FOUND"));
        assignment.reject(LocalDateTime.now(), reason);
        assignmentRepository.update(assignment);
        realtimeNotifications.notifyCoordinatorsAssignmentRejected(assignment, assignment.getUser(), reason);
    }
}