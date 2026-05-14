package RUT.PlanningFlow.domain.service.matching.filter;

import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.service.matching.evaluation.GeoScoreEvaluator;
import RUT.PlanningFlow.domain.service.matching.evaluation.IScoreEvaluator;
import RUT.PlanningFlow.domain.service.matching.evaluation.ScoreDimension;
import RUT.PlanningFlow.domain.service.matching.evaluation.SkillScoreEvaluator;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeightedScoreCandidateFilterConstructionTest {

    @Test
    void null_evaluator_map_rejected() {
        assertThatThrownBy(() -> new WeightedScoreCandidateFilter(null))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "SCORE_FILTER_EVALUATORS_REQUIRED");
    }

    @Test
    void missing_dimension_entry_rejected() {
        final Map<ScoreDimension, IScoreEvaluator> incomplete = Map.of(ScoreDimension.SKILL, new SkillScoreEvaluator());

        assertThatThrownBy(() -> new WeightedScoreCandidateFilter(incomplete))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "SCORE_FILTER_EVALUATOR_MISSING");
    }

    @Test
    void null_evaluator_for_dimension_rejected() {
        final EnumMap<ScoreDimension, IScoreEvaluator> map = new EnumMap<>(ScoreDimension.class);
        map.put(ScoreDimension.SKILL, new SkillScoreEvaluator());
        map.put(ScoreDimension.GEO, null);
        map.put(ScoreDimension.LOAD, new GeoScoreEvaluator());

        assertThatThrownBy(() -> new WeightedScoreCandidateFilter(map))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "SCORE_FILTER_EVALUATOR_MISSING");
    }
}
