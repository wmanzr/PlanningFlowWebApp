package RUT.PlanningFlow.application.dto.matching;

import java.util.List;

public record RankedCandidateResponseDto(
        Integer candidateId,
        String candidateUsername,
        String candidateFullName,
        ScoreBreakdownResponseDto score,
        int rank,
        Double distanceMeters,
        long workedTodayMinutes,
        long maxDailyLoadMinutes,
        List<Integer> matchedRequiredSkillIds
) {
    public RankedCandidateResponseDto {
        matchedRequiredSkillIds = matchedRequiredSkillIds == null ? List.of() : List.copyOf(matchedRequiredSkillIds);
    }
}