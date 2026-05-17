package RUT.PlanningFlow.application.dto.matching;

import java.util.List;

public record MatchTaskResponseDto(
        Integer taskId,
        int requiredCount,
        List<RankedCandidateResponseDto> ranked,
        List<RejectedCandidateResponseDto> rejected,
        int shortageCount
) {
    public MatchTaskResponseDto {
        ranked = ranked == null ? List.of() : List.copyOf(ranked);
        rejected = rejected == null ? List.of() : List.copyOf(rejected);
    }
}