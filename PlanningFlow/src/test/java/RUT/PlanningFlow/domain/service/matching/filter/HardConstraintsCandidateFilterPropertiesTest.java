package RUT.PlanningFlow.domain.service.matching.filter;

import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import RUT.PlanningFlow.domain.vo.EventMode;
import RUT.PlanningFlow.domain.vo.MatchingDistance;
import RUT.PlanningFlow.domain.vo.ScheduleInterval;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.LongRange;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HardConstraintsCandidateFilterPropertiesTest {

    private final HardConstraintsCandidateFilter filter = new HardConstraintsCandidateFilter();

    @Property(tries = 200)
    void reject_or_null_does_not_throw_for_constructed_domain_examples(
            @ForAll @LongRange(min = 0L, max = 72L * 3600 * 1000L) final long gapMillis,
            @ForAll @LongRange(min = 1L, max = 72L * 3600 * 1000L) final long maxDailyMillis,
            @ForAll @LongRange(min = 0L, max = 72L * 3600 * 1000L) final long workedMillis,
            @ForAll final boolean occupiedPresent,
            @ForAll @LongRange(min = -24L * 3600 * 1000L, max = 24L * 3600 * 1000L) final long occupiedOffsetMillis
    ) {
        final User creator = DomainFixtures.user(1);
        final User candidate = DomainFixtures.user(99);
        final var event = DomainFixtures.event(1, creator);
        final LocalDateTime t0 = DomainFixtures.EVENT_RANGE_START.plusHours(5);
        final LocalDateTime t1 = t0.plusHours(3);
        final Task task = DomainFixtures.openTask(44, event, creator, t0, t1);
        task.addRequiredSkill(DomainFixtures.skill(7, "Medic", "Medical"));

        final LocalDateTime occupiedUntil = occupiedPresent ? t0.plus(Duration.ofMillis(occupiedOffsetMillis)) : null;
        final List<ScheduleInterval> intervals = occupiedUntil == null
                ? List.of()
                : List.of(new ScheduleInterval(occupiedUntil.minusHours(1), occupiedUntil));

        final Map<String, Double> categories = new HashMap<>();
        categories.put("medical", 1.0d);

        final CandidateSnapshot snapshot = new CandidateSnapshot(
                occupiedUntil,
                intervals,
                occupiedUntil,
                null,
                Duration.ZERO,
                Duration.ofMillis(workedMillis),
                Map.of(7, 1.0d),
                categories
        );

        final Duration gap = Duration.ofMillis(Math.abs(gapMillis));
        final Duration maxDaily = Duration.ofMillis(Math.max(1L, maxDailyMillis));
        final EventMode mode = new EventMode(MatchingMode.STANDARD, MatchingDistance.BUILDING_SCALE, DomainFixtures.workloadPolicy(maxDaily, gap));
        final MatchingContext context = DomainFixtures.matchingContext(t0.minusHours(2), mode, Map.of(99, snapshot));

        filter.rejectOrNull(task, candidate, context);
    }
}
