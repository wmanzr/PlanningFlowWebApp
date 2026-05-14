package RUT.PlanningFlow.application.dto.matching;

import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.RankedCandidate;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static RankedCandidateResponseDto from(
            final RankedCandidate rc,
            final Task task,
            final Set<Integer> requiredSkillIds,
            final long maxDailyLoadMinutes,
            final Map<Integer, CandidateSnapshot> snapshots
    ) {
        final User user = rc.candidate();
        final CandidateSnapshot snapshot = MatchTaskResponseDto.Projection.snapshotFor(user, snapshots);
        return new RankedCandidateResponseDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                ScoreBreakdownResponseDto.from(rc.score()),
                rc.rank(),
                MatchTaskResponseDto.Projection.distanceMetersOrNull(snapshot, task),
                MatchTaskResponseDto.Projection.toMinutes(snapshot.workedToday()),
                maxDailyLoadMinutes,
                MatchTaskResponseDto.Projection.matchedRequiredSkillIds(requiredSkillIds, snapshot)
        );
    }
}