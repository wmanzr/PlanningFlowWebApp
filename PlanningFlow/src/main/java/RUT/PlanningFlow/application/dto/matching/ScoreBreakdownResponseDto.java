package RUT.PlanningFlow.application.dto.matching;

import RUT.PlanningFlow.domain.service.matching.model.ScoreBreakdown;

public record ScoreBreakdownResponseDto(
        double totalScore,
        double skillScore,
        double geoScore,
        double loadScore
) {
    public static ScoreBreakdownResponseDto from(final ScoreBreakdown score) {
        if (score == null) {
            return new ScoreBreakdownResponseDto(0d, 0d, 0d, 0d);
        }
        return new ScoreBreakdownResponseDto(score.totalScore(), score.skillScore(), score.geoScore(), score.loadScore());
    }
}
