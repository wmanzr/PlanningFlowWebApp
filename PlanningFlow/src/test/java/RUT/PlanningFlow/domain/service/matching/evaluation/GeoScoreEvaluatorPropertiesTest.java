package RUT.PlanningFlow.domain.service.matching.evaluation;

import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
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
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GeoScoreEvaluatorPropertiesTest {

    private final GeoScoreEvaluator evaluator = new GeoScoreEvaluator();

    @Property(tries = 400)
    void evaluate_returns_score_between_zero_and_one(
            @ForAll @DoubleRange(min = -90.0, max = 90.0) final double latFrom,
            @ForAll @DoubleRange(min = -180.0, max = 180.0) final double lonFrom,
            @ForAll @DoubleRange(min = -90.0, max = 90.0) final double latTo,
            @ForAll @DoubleRange(min = -180.0, max = 180.0) final double lonTo,
            @ForAll @DoubleRange(min = 1e-3, max = 500_000.0) @Scale(5) final double radiusMeters
    ) {
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final LocalDateTime t0 = DomainFixtures.EVENT_RANGE_START.plusHours(2);
        final LocalDateTime t1 = t0.plusHours(2);
        final Task task = DomainFixtures.openTask(1, event, creator, t0, t1);
        task.updateLocation(new GeoPoint(latTo, lonTo));

        final GeoPoint from = new GeoPoint(latFrom, lonFrom);
        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(),
                null,
                from,
                Duration.ZERO,
                Duration.ZERO,
                Map.of(),
                Map.of(),
                Map.of());
        final EventMode mode = new EventMode(MatchingMode.STANDARD, new MatchingDistance(radiusMeters), WorkloadPolicy.defaults());
        final MatchingContext context = DomainFixtures.matchingContext(t0.minusHours(1), mode, Map.of(1, snapshot));

        final double score = evaluator.evaluate(task, snapshot, context);

        assertThat(score).isBetween(0.0d, 1.0d);
    }
}
