package RUT.PlanningFlow.domain.service.matching.evaluation;

import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import RUT.PlanningFlow.domain.vo.EventMode;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

import java.time.Duration;
import java.util.List;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SkillScoreEvaluatorPropertiesTest {

    private final SkillScoreEvaluator evaluator = new SkillScoreEvaluator();

    @Property(tries = 300)
    void average_skill_score_stays_in_unit_interval_when_weights_are_normalized(
            @ForAll @IntRange(min = 0, max = 5) final int requiredCount,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) final double w1,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) final double w2
    ) {
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final LocalDateTime t0 = DomainFixtures.EVENT_RANGE_START.plusHours(2);
        final LocalDateTime t1 = t0.plusHours(2);
        final Task task = DomainFixtures.openTask(1, event, creator, t0, t1);

        final Map<Integer, Double> exact = new HashMap<>();
        final Map<String, Double> cumulative = new HashMap<>();
        cumulative.put("alpha", w1);
        cumulative.put("beta", w2);

        int skillIndex = 1;
        for (int i = 0; i < requiredCount; i++) {
            final Skill skill = DomainFixtures.skill(skillIndex, "s" + skillIndex, i % 2 == 0 ? "Alpha" : "Beta");
            task.addRequiredSkill(skill);
            exact.put(skillIndex, i % 2 == 0 ? w1 : w2);
            skillIndex++;
        }

        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                Duration.ZERO,
                exact,
                cumulative,
                Map.of());
        final EventMode defaults = DomainFixtures.defaultEventMode();
        final EventMode mode = DomainFixtures.eventMode(MatchingMode.CRITICAL, defaults.maxGeographicDistance(), defaults.workloadPolicy());
        final MatchingContext context = DomainFixtures.matchingContext(t0.minusHours(1), mode, Map.of(1, snapshot));

        final double score = evaluator.evaluate(task, snapshot, context);

        assertThat(score).isBetween(0.0d, 1.0d);
    }
}
