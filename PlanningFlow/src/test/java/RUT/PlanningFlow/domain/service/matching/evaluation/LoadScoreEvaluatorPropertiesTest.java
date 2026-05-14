package RUT.PlanningFlow.domain.service.matching.evaluation;

import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import RUT.PlanningFlow.domain.vo.EventMode;
import RUT.PlanningFlow.domain.vo.MatchingDistance;
import RUT.PlanningFlow.domain.vo.WorkloadPolicy;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.LongRange;

import java.time.Duration;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LoadScoreEvaluatorPropertiesTest {

    private final LoadScoreEvaluator evaluator = new LoadScoreEvaluator();

    @Property(tries = 400)
    void evaluate_returns_score_between_zero_and_one_when_inputs_are_domain_valid(
            @ForAll @LongRange(min = 0L, max = 48L * 3600 * 1000L) final long workedMillis,
            @ForAll @LongRange(min = 1L, max = 72L * 3600 * 1000L) final long maxDailyMillis
    ) {
        final Duration worked = Duration.ofMillis(workedMillis);
        final Duration maxDaily = Duration.ofMillis(maxDailyMillis);

        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final LocalDateTime t0 = DomainFixtures.EVENT_RANGE_START.plusHours(2);
        final LocalDateTime t1 = t0.plusHours(2);
        final Task task = DomainFixtures.openTask(1, event, creator, t0, t1);

        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                worked,
                Map.of(),
                Map.of()
        );
        final WorkloadPolicy policy = DomainFixtures.workloadPolicy(maxDaily, Duration.ofMinutes(5));
        final EventMode mode = DomainFixtures.eventMode(MatchingMode.STANDARD, MatchingDistance.BUILDING_SCALE, policy);
        final MatchingContext context = DomainFixtures.matchingContext(t0.minusHours(1), mode, Map.of(1, snapshot));

        final double score = evaluator.evaluate(task, snapshot, context);

        assertThat(score).isBetween(0.0d, 1.0d);
    }
}
