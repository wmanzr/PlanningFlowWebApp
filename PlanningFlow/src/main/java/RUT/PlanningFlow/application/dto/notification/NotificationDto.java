package RUT.PlanningFlow.application.dto.notification;

import java.time.LocalDateTime;

public record NotificationDto(
        Integer id,
        String title,
        String message,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {}

