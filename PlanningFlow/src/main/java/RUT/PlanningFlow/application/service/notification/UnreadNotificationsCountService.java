package RUT.PlanningFlow.application.service.notification;

import RUT.PlanningFlow.application.port.in.notification.UnreadNotificationsCountQuery;
import RUT.PlanningFlow.application.port.out.repository.NotificationRepositoryPort;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UnreadNotificationsCountService implements UnreadNotificationsCountQuery {

    private final NotificationRepositoryPort notifications;

    public UnreadNotificationsCountService(final NotificationRepositoryPort notifications) {
        DomainAssert.notNull(notifications, "Репозиторий уведомлений обязателен", "NOTIFICATION_REPOSITORY_REQUIRED");
        this.notifications = notifications;
    }

    @Override
    public long execute(final Integer callerUserId) {
        DomainAssert.notNull(callerUserId, "Идентификатор пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        return notifications.countUnread(callerUserId);
    }
}

