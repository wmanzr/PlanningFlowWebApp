package RUT.PlanningFlow.application.dto.matching;

import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.RejectedCandidate;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static RejectedCandidateResponseDto from(
            final RejectedCandidate rejected,
            final Task task,
            final Set<Integer> requiredSkillIds,
            final long maxDailyLoadMinutes,
            final Map<Integer, CandidateSnapshot> snapshots
    ) {
        final User user = rejected.candidate();
        final CandidateSnapshot snapshot = MatchTaskResponseDto.Projection.snapshotFor(user, snapshots);
        return new RejectedCandidateResponseDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                rejected.reason().name(),
                rejected.details(),
                MatchTaskResponseDto.Projection.distanceMetersOrNull(snapshot, task),
                MatchTaskResponseDto.Projection.toMinutes(snapshot.workedToday()),
                maxDailyLoadMinutes,
                MatchTaskResponseDto.Projection.matchedRequiredSkillIds(requiredSkillIds, snapshot)
        );
    }
}
