package RUT.PlanningFlow.domain.vo;

import RUT.PlanningFlow.domain.utils.DomainAssert;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public final class DateTimeRange {
    private final LocalDateTime start;
    private final LocalDateTime end;

    public DateTimeRange(final LocalDateTime start, final LocalDateTime end) {
        DomainAssert.notNull(start, "Дата начала обязательна", "DATE_START_REQUIRED");
        DomainAssert.notNull(end, "Дата окончания обязательна", "DATE_END_REQUIRED");
        DomainAssert.isTrue(!start.isAfter(end), "Некорректный временной интервал", "INVALID_DATE_RANGE");
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public Duration duration() {
        return Duration.between(start, end);
    }

    public boolean overlaps(final DateTimeRange other) {
        DomainAssert.notNull(other, "Сравниваемый интервал обязателен", "DATE_RANGE_REQUIRED");
        return !this.end.isBefore(other.start) && !this.start.isAfter(other.end);
    }

    public boolean contains(final LocalDateTime point) {
        DomainAssert.notNull(point, "Момент времени обязателен", "DATE_TIME_REQUIRED");
        return !point.isBefore(start) && !point.isAfter(end);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DateTimeRange other)) {
            return false;
        }
        return Objects.equals(start, other.start) && Objects.equals(end, other.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}