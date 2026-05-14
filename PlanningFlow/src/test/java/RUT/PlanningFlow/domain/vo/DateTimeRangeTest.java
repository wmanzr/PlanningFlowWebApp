package RUT.PlanningFlow.domain.vo;

import RUT.PlanningFlow.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateTimeRangeTest {

    @Test
    void duration_between_start_and_end() {
        final LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);
        final LocalDateTime end = LocalDateTime.of(2026, 1, 1, 12, 30);

        final DateTimeRange range = new DateTimeRange(start, end);

        assertThat(range.duration().toMinutes()).isEqualTo(150);
    }

    @Test
    void overlaps_when_ranges_intersect() {
        final DateTimeRange a = new DateTimeRange(
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 12, 0)
        );
        final DateTimeRange b = new DateTimeRange(
                LocalDateTime.of(2026, 1, 1, 11, 0),
                LocalDateTime.of(2026, 1, 1, 13, 0)
        );

        assertThat(a.overlaps(b)).isTrue();
    }

    @Test
    void invalid_range_start_after_end() {
        final LocalDateTime start = LocalDateTime.of(2026, 1, 1, 14, 0);
        final LocalDateTime end = LocalDateTime.of(2026, 1, 1, 10, 0);

        assertThatThrownBy(() -> new DateTimeRange(start, end))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_DATE_RANGE");
    }

    @Test
    void no_overlap_when_ranges_are_separated() {
        final DateTimeRange a = new DateTimeRange(
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 11, 0)
        );
        final DateTimeRange b = new DateTimeRange(
                LocalDateTime.of(2026, 1, 1, 12, 0),
                LocalDateTime.of(2026, 1, 1, 13, 0)
        );

        assertThat(a.overlaps(b)).isFalse();
    }

    @Test
    void contains_point_inclusive() {
        final LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);
        final LocalDateTime end = LocalDateTime.of(2026, 1, 1, 12, 0);
        final DateTimeRange range = new DateTimeRange(start, end);

        assertThat(range.contains(start)).isTrue();
        assertThat(range.contains(end)).isTrue();
        assertThat(range.contains(start.plusHours(3))).isFalse();
    }
}
