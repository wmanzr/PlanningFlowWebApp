package RUT.PlanningFlow.domain.service.matching.evaluation;

import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import RUT.PlanningFlow.domain.vo.EventMode;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import RUT.PlanningFlow.domain.vo.MatchingDistance;
import RUT.PlanningFlow.domain.vo.WorkloadPolicy;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingEvaluatorsBranchCoverageTest {

    private static final LocalDateTime T0 = DomainFixtures.EVENT_RANGE_START.plusHours(4);
    private static final LocalDateTime T1 = T0.plusHours(2);

    @Test
    void geo_mid_score_when_previous_location_missing() {
        final GeoScoreEvaluator geo = new GeoScoreEvaluator();
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(1, event, creator, T0, T1);
        task.updateLocation(DomainFixtures.moscowCenter());
        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                Duration.ZERO,
                Map.of(),
                Map.of(),
                Map.of());
        final MatchingContext ctx = DomainFixtures.matchingContext(T0.minusHours(1), DomainFixtures.defaultEventMode(), Map.of(1, snapshot));

        assertThat(geo.evaluate(task, snapshot, ctx)).isEqualTo(0.5);
    }

    @Test
    void geo_when_radius_non_positive_uses_edge_scores() {
        final GeoScoreEvaluator geo = new GeoScoreEvaluator();
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(1, event, creator, T0, T1);
        task.updateLocation(new GeoPoint(55.75, 37.61));
        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(),
                null,
                new GeoPoint(55.75, 37.61),
                Duration.ZERO,
                Duration.ZERO,
                Map.of(),
                Map.of(),
                Map.of());
        final EventMode mode = new EventMode(MatchingMode.STANDARD, new MatchingDistance(1.0), WorkloadPolicy.defaults());
        final MatchingContext ctx = DomainFixtures.matchingContext(T0.minusHours(1), mode, Map.of(1, snapshot));

        assertThat(geo.evaluate(task, snapshot, ctx)).isEqualTo(1.0);

        final Task taskFar = DomainFixtures.openTask(2, event, creator, T0, T1);
        taskFar.updateLocation(new GeoPoint(56.0, 38.0));
        assertThat(geo.evaluate(taskFar, snapshot, ctx)).isZero();
    }

    @Test
    void load_zero_when_max_daily_is_zero() {
        final LoadScoreEvaluator load = new LoadScoreEvaluator();
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(1, event, creator, T0, T1);
        final CandidateSnapshot snapshot = CandidateSnapshot.empty();
        final WorkloadPolicy policy = DomainFixtures.workloadPolicy(Duration.ZERO, Duration.ofMinutes(5));
        final EventMode mode = DomainFixtures.eventMode(MatchingMode.STANDARD, MatchingDistance.BUILDING_SCALE, policy);
        final MatchingContext ctx = DomainFixtures.matchingContext(T0.minusHours(1), mode, Map.of(1, snapshot));

        assertThat(load.evaluate(task, snapshot, ctx)).isZero();
    }

    @Test
    void skill_treats_null_required_skill_as_zero_contribution() {
        final SkillScoreEvaluator skillEval = new SkillScoreEvaluator();
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final Skill real = DomainFixtures.skill(9, "X", "Cat");
        final Task task = new Task(
                1,
                event,
                creator,
                "t",
                TaskStatus.OPEN,
                T0,
                T1,
                null,
                Arrays.asList(real, null),
                java.util.List.of()
        );
        final CandidateSnapshot snapshot = CandidateSnapshot.empty();
        final MatchingContext ctx = DomainFixtures.matchingContext(T0.minusHours(1), DomainFixtures.defaultEventMode(), Map.of(1, snapshot));

        assertThat(skillEval.evaluate(task, snapshot, ctx)).isZero();
    }

    @Test
    void skill_standard_mode_without_exact_match_returns_zero_per_skill() {
        final SkillScoreEvaluator skillEval = new SkillScoreEvaluator();
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(1, event, creator, T0, T1);
        task.addRequiredSkill(DomainFixtures.skill(77, "Need", "Medical"));
        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                Duration.ZERO,
                Map.of(),
                Map.of("medical", 1.0),
                Map.of());
        final MatchingContext ctx = DomainFixtures.matchingContext(T0.minusHours(1), DomainFixtures.defaultEventMode(), Map.of(1, snapshot));

        assertThat(skillEval.evaluate(task, snapshot, ctx)).isZero();
    }
}
