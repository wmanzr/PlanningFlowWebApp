package RUT.PlanningFlow.domain.service.matching.evaluation;

import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;

import java.time.Duration;

public final class LoadScoreEvaluator implements IScoreEvaluator {

    private static final double MIN_SCORE = 0.0d;

    @Override
    public double evaluate(
            final Task task,
            final CandidateSnapshot snapshot,
            final MatchingContext context
    ) {
        final Duration maxDailyLoad = context.eventMode().workloadPolicy().maxDailyLoad();
        if (maxDailyLoad.isZero()) {
            return MIN_SCORE;
        }
        final Duration worked = snapshot.workedToday() == null ? Duration.ZERO : snapshot.workedToday();
        if (worked.compareTo(maxDailyLoad) >= 0) {
            return MIN_SCORE;
        }
        final Duration remaining = maxDailyLoad.minus(worked);
        return remaining.toMillis() / (double) maxDailyLoad.toMillis();
    }
}