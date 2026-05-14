package RUT.PlanningFlow.domain.service.matching.evaluation;

import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;

import java.util.List;

public final class SkillScoreEvaluator implements IScoreEvaluator {

    private static final double MAX_SCORE = 1.0d;

    @Override
    public double evaluate(
            final Task task,
            final CandidateSnapshot snapshot,
            final MatchingContext context
    ) {
        final List<Skill> required = task.getRequiredSkills();
        if (required.isEmpty()) {
            return MAX_SCORE;
        }

        final MatchingMode mode = context.eventMode().matchingMode();
        double sum = 0.0d;
        for (final Skill requiredSkill : required) {
            sum += scoreForRequiredSkill(requiredSkill, snapshot, mode);
        }
        return sum / required.size();
    }

    private static double scoreForRequiredSkill(
            final Skill required,
            final CandidateSnapshot snapshot,
            final MatchingMode mode
    ) {
        if (required == null) {
            return 0.0d;
        }

        final Integer requiredId = required.getId();
        if (requiredId != null) {
            final Double exact = snapshot.exactSkillWeights().get(requiredId);
            if (exact != null) {
                return exact;
            }
        }

        if (mode == MatchingMode.STANDARD) {
            return 0.0d;
        }

        final String category = CandidateSnapshot.normalizedCategory(required.getCategory());
        final Double cumulative = snapshot.cumulativeCategoryWeights().get(category);
        if (cumulative == null) {
            return 0.0d;
        }
        return cumulative * mode.getRelatedSkillFactor();
    }
}
