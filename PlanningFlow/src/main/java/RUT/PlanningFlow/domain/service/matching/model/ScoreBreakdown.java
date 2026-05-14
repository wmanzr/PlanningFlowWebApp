package RUT.PlanningFlow.domain.service.matching.model;

public record ScoreBreakdown(
        double totalScore,
        double skillScore,
        double geoScore,
        double loadScore
) {
}