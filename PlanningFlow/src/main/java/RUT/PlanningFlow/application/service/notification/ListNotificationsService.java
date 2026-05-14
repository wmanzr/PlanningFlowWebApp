package RUT.PlanningFlow.application.service.notification;

import RUT.PlanningFlow.application.dto.notification.NotificationDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.notification.ListNotificationsQuery;
import RUT.PlanningFlow.application.port.out.repository.NotificationRepositoryPort;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListNotificationsService implements ListNotificationsQuery {

    private final NotificationRepositoryPort notifications;

    public ListNotificationsService(final NotificationRepositoryPort notifications) {
        DomainAssert.notNull(notifications, "Репозиторий уведомлений обязателен", "NOTIFICATION_REPOSITORY_REQUIRED");
        this.notifications = notifications;
    }

    @Override
    public PageResult<NotificationDto> execute(final Integer callerUserId, final String filter, final PageQuery pageQuery) {
        DomainAssert.notNull(callerUserId, "Идентификатор пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        return notifications.list(callerUserId, filter, pageQuery);
    }
}

