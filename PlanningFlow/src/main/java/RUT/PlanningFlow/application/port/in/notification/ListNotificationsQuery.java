package RUT.PlanningFlow.application.port.in.notification;

import RUT.PlanningFlow.application.dto.notification.NotificationDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;

public interface ListNotificationsQuery {
    PageResult<NotificationDto> execute(Integer callerUserId, String filter, PageQuery pageQuery);
}

