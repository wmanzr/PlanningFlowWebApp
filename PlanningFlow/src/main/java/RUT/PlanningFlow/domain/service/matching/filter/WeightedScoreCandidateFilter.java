package RUT.PlanningFlow.domain.service.matching.filter;

import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.evaluation.IScoreEvaluator;
import RUT.PlanningFlow.domain.service.matching.evaluation.ScoreDimension;
import RUT.PlanningFlow.domain.service.matching.evaluation.GeoScoreEvaluator;
import RUT.PlanningFlow.domain.service.matching.evaluation.LoadScoreEvaluator;
import RUT.PlanningFlow.domain.service.matching.evaluation.SkillScoreEvaluator;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.service.matching.model.ScoreBreakdown;
import RUT.PlanningFlow.domain.service.matching.strategy.WeightingStrategies;
import RUT.PlanningFlow.domain.service.matching.strategy.WeightingStrategy;
import RUT.PlanningFlow.domain.utils.DomainAssert;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class WeightedScoreCandidateFilter implements CandidatePhaseBFilter {

    private final EnumMap<ScoreDimension, IScoreEvaluator> evaluators;

    public WeightedScoreCandidateFilter(final Map<ScoreDimension, IScoreEvaluator> evaluators) {
        DomainAssert.notNull(evaluators, "Набор оценщиков обязателен", "SCORE_FILTER_EVALUATORS_REQUIRED");
        for (ScoreDimension dimension : ScoreDimension.values()) {
            DomainAssert.notNull(evaluators.get(dimension), "Оценщик обязателен для " + dimension, "SCORE_FILTER_EVALUATOR_MISSING");
        }
        this.evaluators = new EnumMap<>(evaluators);
    }

    public static WeightedScoreCandidateFilter balancedDefaults() {
        return new WeightedScoreCandidateFilter(Map.of(
                ScoreDimension.SKILL, new SkillScoreEvaluator(),
                ScoreDimension.GEO, new GeoScoreEvaluator(),
                ScoreDimension.LOAD, new LoadScoreEvaluator()
        ));
    }

    @Override
    public ScoreBreakdown score(final Task task, final User candidate, final MatchingContext context) {
        DomainAssert.notNull(task, "Задача обязательна для скоринга", "MATCHING_TASK_REQUIRED");
        DomainAssert.notNull(candidate, "Кандидат обязателен для скоринга", "MATCHING_CANDIDATE_REQUIRED");
        DomainAssert.notNull(context, "Контекст подбора обязателен", "MATCHING_CONTEXT_REQUIRED");

        final WeightingStrategy weights = WeightingStrategies.forMode(context.eventMode().matchingMode());
        validateWeights(weights);

        final CandidateSnapshot snapshot = context.snapshotFor(candidate);

        final double skillScore = Objects.requireNonNull(evaluators.get(ScoreDimension.SKILL))
                .evaluate(task, snapshot, context);
        final double geoScore = Objects.requireNonNull(evaluators.get(ScoreDimension.GEO))
                .evaluate(task, snapshot, context);
        final double loadScore = Objects.requireNonNull(evaluators.get(ScoreDimension.LOAD))
                .evaluate(task, snapshot, context);

        final double totalScore =
                skillScore * weights.getSkillWeight()
                        + geoScore * weights.getGeoWeight()
                        + loadScore * weights.getLoadWeight();

        return new ScoreBreakdown(totalScore, skillScore, geoScore, loadScore);
    }

    private static void validateWeights(final WeightingStrategy weights) {
        final double skill = weights.getSkillWeight();
        final double geo = weights.getGeoWeight();
        final double load = weights.getLoadWeight();
        DomainAssert.isTrue(
                inUnitInterval(skill) && inUnitInterval(geo) && inUnitInterval(load),
                "Весовые коэффициенты должны быть в диапазоне [0;1]",
                "INVALID_WEIGHTING_COMPONENT"
        );
        final double sum = skill + geo + load;
        final double epsilon = 1.0e-9d;
        DomainAssert.isTrue(
                Math.abs(sum - 1.0d) <= epsilon,
                "Сумма весовых коэффициентов должна быть равна 1.0",
                "INVALID_WEIGHTING_SUM"
        );
    }

    private static boolean inUnitInterval(final double value) {
        return value >= 0.0d && value <= 1.0d;
    }
}