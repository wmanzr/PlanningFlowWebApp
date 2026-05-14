package RUT.PlanningFlow.domain.service.scheduling;

import RUT.PlanningFlow.domain.vo.ScheduleInterval;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleConflictPolicyTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 6, 1, 10, 0);
    private static final Duration GAP = Duration.ofMinutes(15);

    @Test
    void detects_overlap_on_same_day() {
        final boolean conflict = ScheduleConflictPolicy.intervalsConflict(
                T0,
                T0.plusHours(2),
                T0.plusHours(1),
                T0.plusHours(3),
                GAP
        );

        assertThat(conflict).isTrue();
    }

    @Test
    void allows_non_overlapping_slots_on_same_day_with_gap() {
        final boolean conflict = ScheduleConflictPolicy.intervalsConflict(
                T0,
                T0.plusHours(1),
                T0.plusHours(1).plus(GAP),
                T0.plusHours(2),
                GAP
        );

        assertThat(conflict).isFalse();
    }

    @Test
    void allows_slot_on_next_day() {
        final boolean conflict = ScheduleConflictPolicy.intervalsConflict(
                T0,
                T0.plusHours(2),
                T0.plusDays(1),
                T0.plusDays(1).plusHours(2),
                GAP
        );

        assertThat(conflict).isFalse();
    }

    @Test
    void rejects_when_gap_is_insufficient() {
        final boolean conflict = ScheduleConflictPolicy.intervalsConflict(
                T0,
                T0.plusHours(1),
                T0.plusHours(1).plusMinutes(10),
                T0.plusHours(2),
                GAP
        );

        assertThat(conflict).isTrue();
    }

    @Test
    void conflictsWithCommitted_ignores_empty_list() {
        assertThat(ScheduleConflictPolicy.conflictsWithCommitted(
                T0,
                T0.plusHours(1),
                List.of(),
                GAP
        )).isFalse();
    }

    @Test
    void conflictsWithCommitted_checks_all_slots() {
        final List<ScheduleInterval> committed = List.of(
                new ScheduleInterval(T0.minusHours(4), T0.minusHours(2)),
                new ScheduleInterval(T0.plusHours(3), T0.plusHours(5))
        );

        assertThat(ScheduleConflictPolicy.conflictsWithCommitted(
                T0,
                T0.plusHours(2),
                committed,
                GAP
        )).isFalse();

        assertThat(ScheduleConflictPolicy.conflictsWithCommitted(
                T0.plusHours(2).plusMinutes(30),
                T0.plusHours(4),
                committed,
                GAP
        )).isTrue();
    }
}
