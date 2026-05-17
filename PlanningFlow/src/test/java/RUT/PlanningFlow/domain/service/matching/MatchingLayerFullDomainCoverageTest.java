package RUT.PlanningFlow.domain.service.matching;

import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.evaluation.GeoScoreEvaluator;
import RUT.PlanningFlow.domain.service.matching.evaluation.LoadScoreEvaluator;
import RUT.PlanningFlow.domain.service.matching.evaluation.SkillScoreEvaluator;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import RUT.PlanningFlow.domain.vo.EventMode;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import RUT.PlanningFlow.domain.vo.MatchingDistance;
import RUT.PlanningFlow.domain.vo.WorkloadPolicy;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchingLayerFullDomainCoverageTest {

    private static final LocalDateTime T0 = DomainFixtures.EVENT_RANGE_START.plusHours(3);
    private static final LocalDateTime T1 = T0.plusHours(2);

    @Test
    void matching_engine_requires_task_and_candidate_list() {
        final MatchingEngine engine = new MatchingEngine();
        final Task task = DomainFixtures.openTask(1, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);
        final MatchingContext ctx = DomainFixtures.matchingContext(
                LocalDateTime.now(),
                DomainFixtures.defaultEventMode(),
                Map.of()
        );

        assertThatThrownBy(() -> engine.match(null, List.of(), 1, ctx))
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_TASK_REQUIRED");

        assertThatThrownBy(() -> engine.match(task, null, 1, ctx))
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_CANDIDATES_REQUIRED");
    }

    @Test
    void geo_evaluator_mid_score_when_task_has_no_location() {
        final GeoScoreEvaluator geo = new GeoScoreEvaluator();
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(1, event, creator, T0, T1);
        final CandidateSnapshot snap = new CandidateSnapshot(
                null,
                List.of(),
                null,
                new GeoPoint(55.0, 37.0),
                Duration.ZERO,
                Duration.ZERO,
                Map.of(),
                Map.of(),
                Map.of());
        final MatchingContext ctx = DomainFixtures.matchingContext(T0.minusHours(1), DomainFixtures.defaultEventMode(), Map.of(1, snap));

        assertThat(geo.evaluate(task, snap, ctx)).isEqualTo(0.5);
    }

    @Test
    void load_evaluator_returns_zero_when_worked_reaches_max() {
        final LoadScoreEvaluator load = new LoadScoreEvaluator();
        final User creator = DomainFixtures.user(1);
        final Task task = DomainFixtures.openTask(1, DomainFixtures.event(1, creator), creator, T0, T1);
        final Duration max = Duration.ofHours(8);
        final CandidateSnapshot snap = new CandidateSnapshot(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                max,
                Map.of(),
                Map.of(),
                Map.of());
        final WorkloadPolicy policy = DomainFixtures.workloadPolicy(max, Duration.ofMinutes(10));
        final EventMode mode = DomainFixtures.eventMode(MatchingMode.STANDARD, MatchingDistance.BUILDING_SCALE, policy);
        final MatchingContext ctx = DomainFixtures.matchingContext(T0.minusHours(1), mode, Map.of(1, snap));

        assertThat(load.evaluate(task, snap, ctx)).isZero();
    }

    @Test
    void skill_evaluator_critical_uses_category_weight_times_factor() {
        final SkillScoreEvaluator eval = new SkillScoreEvaluator();
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(1, event, creator, T0, T1);
        task.addRequiredSkill(DomainFixtures.skill(99, "Need", "Medical"));
        final CandidateSnapshot snap = new CandidateSnapshot(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                Duration.ZERO,
                Map.of(),
                Map.of("medical", 1.0),
                Map.of());
        final EventMode defaults = DomainFixtures.defaultEventMode();
        final EventMode criticalMode = DomainFixtures.eventMode(
                MatchingMode.CRITICAL,
                defaults.maxGeographicDistance(),
                defaults.workloadPolicy()
        );
        final MatchingContext ctx = DomainFixtures.matchingContext(T0.minusHours(1), criticalMode, Map.of(1, snap));

        assertThat(eval.evaluate(task, snap, ctx)).isEqualTo(MatchingMode.CRITICAL.getRelatedSkillFactor());
    }

}
