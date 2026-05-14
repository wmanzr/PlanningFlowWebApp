package RUT.PlanningFlow.application.service.notification;

import RUT.PlanningFlow.application.port.in.notification.MarkNotificationReadUseCase;
import RUT.PlanningFlow.application.port.out.repository.NotificationRepositoryPort;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarkNotificationReadService implements MarkNotificationReadUseCase {

    private final NotificationRepositoryPort notifications;

    public MarkNotificationReadService(final NotificationRepositoryPort notifications) {
        DomainAssert.notNull(notifications, "Репозиторий уведомлений обязателен", "NOTIFICATION_REPOSITORY_REQUIRED");
        this.notifications = notifications;
    }

    @Override
    public void execute(final Integer callerUserId, final Integer notificationId) {
        DomainAssert.notNull(callerUserId, "Идентификатор пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(notificationId, "ID уведомления обязателен", "NOTIFICATION_ID_REQUIRED");
        notifications.markRead(callerUserId, notificationId);
    }
}

