package RUT.PlanningFlow.application.dto.matching;

import java.util.List;

public record RejectedCandidateResponseDto(
        Integer candidateId,
        String candidateUsername,
        String candidateFullName,
        String reason,
        String details,
        Double distanceMeters,
        long workedTodayMinutes,
        long maxDailyLoadMinutes,
        List<Integer> matchedRequiredSkillIds
) {
    public RejectedCandidateResponseDto {
        matchedRequiredSkillIds = matchedRequiredSkillIds == null ? List.of() : List.copyOf(matchedRequiredSkillIds);
    }
}