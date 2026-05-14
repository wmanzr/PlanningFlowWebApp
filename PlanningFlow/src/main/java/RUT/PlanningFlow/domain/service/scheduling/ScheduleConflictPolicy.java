package RUT.PlanningFlow.domain.service.scheduling;

import RUT.PlanningFlow.domain.utils.DomainAssert;
import RUT.PlanningFlow.domain.vo.ScheduleInterval;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public final class ScheduleConflictPolicy {

    private ScheduleConflictPolicy() {
    }

    public static boolean intervalsConflict(
            final LocalDateTime start1,
            final LocalDateTime end1,
            final LocalDateTime start2,
            final LocalDateTime end2,
            final Duration gap
    ) {
        DomainAssert.notNull(gap, "Минимальный технический разрыв обязателен", "MIN_TECHNICAL_GAP_REQUIRED");
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        final LocalDateTime end1WithGap = end1.plus(gap);
        final LocalDateTime end2WithGap = end2.plus(gap);
        return start1.isBefore(end2WithGap) && start2.isBefore(end1WithGap);
    }

    public static boolean conflictsWithCommitted(
            final LocalDateTime candidateStart,
            final LocalDateTime candidateEnd,
            final List<ScheduleInterval> committedIntervals,
            final Duration gap
    ) {
        if (committedIntervals == null || committedIntervals.isEmpty()) {
            return false;
        }
        for (final ScheduleInterval slot : committedIntervals) {
            if (slot == null) {
                continue;
            }
            if (intervalsConflict(candidateStart, candidateEnd, slot.start(), slot.end(), gap)) {
                return true;
            }
        }
        return false;
    }
}
