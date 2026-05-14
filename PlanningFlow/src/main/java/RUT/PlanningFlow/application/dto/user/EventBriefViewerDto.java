package RUT.PlanningFlow.application.dto.user;

import java.time.LocalDateTime;

public record EventBriefViewerDto(
        Integer eventId,
        String title,
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
