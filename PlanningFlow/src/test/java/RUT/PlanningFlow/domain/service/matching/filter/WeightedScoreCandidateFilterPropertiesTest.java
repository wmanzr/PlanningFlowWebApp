package RUT.PlanningFlow.domain.service.matching.filter;

import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.service.matching.model.ScoreBreakdown;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import RUT.PlanningFlow.domain.vo.EventMode;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import RUT.PlanningFlow.domain.vo.MatchingDistance;
import RUT.PlanningFlow.domain.vo.WorkloadPolicy;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.Scale;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WeightedScoreCandidateFilterPropertiesTest {

    private final WeightedScoreCandidateFilter filter = WeightedScoreCandidateFilter.balancedDefaults();

    @Property(tries = 250)
    void balanced_breakdown_components_and_total_stay_in_unit_interval(
            @ForAll @DoubleRange(min = -90.0, max = 90.0) final double latFrom,
            @ForAll @DoubleRange(min = -180.0, max = 180.0) final double lonFrom,
            @ForAll @DoubleRange(min = -90.0, max = 90.0) final double latTo,
            @ForAll @DoubleRange(min = -180.0, max = 180.0) final double lonTo,
            @ForAll @DoubleRange(min = 1e-3, max = 500_000.0) @Scale(5) final double radiusMeters,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) final double skillExact,
            @ForAll final MatchingMode mode,
            @ForAll @DoubleRange(min = 0.0, max = 8.0) final double workedHours,
            @ForAll @DoubleRange(min = 1.0, max = 12.0) final double maxDailyHours
    ) {
        final User creator = DomainFixtures.user(1);
        final User candidate = DomainFixtures.user(10);
        final var event = DomainFixtures.event(1, creator);
        final LocalDateTime t0 = DomainFixtures.EVENT_RANGE_START.plusHours(2);
        final LocalDateTime t1 = t0.plusHours(2);
        final Task task = DomainFixtures.openTask(1, event, creator, t0, t1);
        task.updateLocation(new GeoPoint(latTo, lonTo));
        task.addRequiredSkill(DomainFixtures.skill(50, "Req", "Medical"));

        final Map<Integer, Double> exact = new HashMap<>();
        exact.put(50, skillExact);

        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(),
                null,
                new GeoPoint(latFrom, lonFrom),
                Duration.ZERO,
                Duration.ofMinutes((long) (workedHours * 60.0)),
                exact,
                Map.of("medical", skillExact)
        );

        final Duration maxDaily = Duration.ofMinutes((long) (maxDailyHours * 60.0));
        final WorkloadPolicy policy = DomainFixtures.workloadPolicy(maxDaily, Duration.ofMinutes(10));
        final EventMode eventMode = new EventMode(mode, new MatchingDistance(radiusMeters), policy);
        final MatchingContext context = DomainFixtures.matchingContext(t0.minusHours(1), eventMode, Map.of(10, snapshot));

        final ScoreBreakdown breakdown = filter.score(task, candidate, context);

        assertThat(breakdown.skillScore()).isBetween(0.0d, 1.0d);
        assertThat(breakdown.geoScore()).isBetween(0.0d, 1.0d);
        assertThat(breakdown.loadScore()).isBetween(0.0d, 1.0d);
        assertThat(breakdown.totalScore()).isBetween(0.0d, 1.0d);
    }
}
