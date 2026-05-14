package RUT.PlanningFlow.domain.service.matching.evaluation;

import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.vo.GeoPoint;

public final class GeoScoreEvaluator implements IScoreEvaluator {

    private static final double MIN_SCORE = 0.0d;
    private static final double MAX_SCORE = 1.0d;

    @Override
    public double evaluate(
            final Task task,
            final CandidateSnapshot snapshot,
            final MatchingContext context
    ) {
        final GeoPoint from = snapshot.previousTaskLocation();
        if (from == null) {
            return (MIN_SCORE + MAX_SCORE) / 2.0d;
        }

        final GeoPoint to = task.getLocation();
        if (to == null) {
            return (MIN_SCORE + MAX_SCORE) / 2.0d;
        }

        final double meters = from.haversineDistanceMetersTo(to);
        final double radius = context.eventMode().maxGeographicDistance().referenceRadiusMeters();
        if (radius <= 0.0) {
            return meters == 0.0 ? MAX_SCORE : MIN_SCORE;
        }
        if (meters >= radius) {
            return MIN_SCORE;
        }
        final double linear = 1.0d - meters / radius;
        return Math.clamp(linear, MIN_SCORE, MAX_SCORE);
    }
}