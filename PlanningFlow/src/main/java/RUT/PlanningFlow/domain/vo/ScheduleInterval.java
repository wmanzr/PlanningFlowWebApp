package RUT.PlanningFlow.domain.vo;

import RUT.PlanningFlow.domain.utils.DomainAssert;

import java.time.LocalDateTime;

public record ScheduleInterval(LocalDateTime start, LocalDateTime end) {
    public ScheduleInterval {
        DomainAssert.notNull(start, "Начало интервала обязательно", "SCHEDULE_INTERVAL_START_REQUIRED");
        DomainAssert.notNull(end, "Конец интервала обязателен", "SCHEDULE_INTERVAL_END_REQUIRED");
        DomainAssert.isTrue(!end.isBefore(start), "Конец интервала не может быть раньше начала", "INVALID_SCHEDULE_INTERVAL");
    }
}
