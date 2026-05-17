package RUT.PlanningFlow.application.dto.matching;

public record ScoreBreakdownResponseDto(
        double totalScore,
        double skillScore,
        double geoScore,
        double loadScore
) {
}