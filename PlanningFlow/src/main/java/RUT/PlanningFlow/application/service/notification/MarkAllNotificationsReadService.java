package RUT.PlanningFlow.application.service.notification;

import RUT.PlanningFlow.application.port.in.notification.MarkAllNotificationsReadUseCase;
import RUT.PlanningFlow.application.port.out.repository.NotificationRepositoryPort;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarkAllNotificationsReadService implements MarkAllNotificationsReadUseCase {

    private final NotificationRepositoryPort notifications;

    public MarkAllNotificationsReadService(final NotificationRepositoryPort notifications) {
        DomainAssert.notNull(notifications, "Репозиторий уведомлений обязателен", "NOTIFICATION_REPOSITORY_REQUIRED");
        this.notifications = notifications;
    }

    @Override
    public void execute(final Integer callerUserId) {
        DomainAssert.notNull(callerUserId, "Идентификатор пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        notifications.markAllRead(callerUserId);
    }
}

