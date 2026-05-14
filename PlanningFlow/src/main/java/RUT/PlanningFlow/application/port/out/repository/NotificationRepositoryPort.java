package RUT.PlanningFlow.application.port.out.repository;

import RUT.PlanningFlow.application.dto.notification.NotificationDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;

import java.time.LocalDateTime;

public interface NotificationRepositoryPort {
    PageResult<NotificationDto> list(Integer userId, String filter, PageQuery pageQuery);
    long countUnread(Integer userId);
    void markRead(Integer userId, Integer notificationId);
    void markAllRead(Integer userId);
    Integer create(Integer userId, String title, String message, LocalDateTime createdAt);
}

