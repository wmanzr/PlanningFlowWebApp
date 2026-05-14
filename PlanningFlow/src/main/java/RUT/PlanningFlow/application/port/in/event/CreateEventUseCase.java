package RUT.PlanningFlow.application.port.in.event;

import RUT.PlanningFlow.domain.vo.GeoPoint;

import java.time.LocalDateTime;

public interface CreateEventUseCase {
    Integer execute(
            String title,
            String description,
            LocalDateTime startDate,
            LocalDateTime endDate,
            GeoPoint location,
            Integer creatorUserId
    );
}

