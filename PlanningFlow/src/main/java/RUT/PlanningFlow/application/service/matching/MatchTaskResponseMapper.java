package RUT.PlanningFlow.application.service.matching;

import RUT.PlanningFlow.application.dto.matching.MatchTaskResponseDto;
import RUT.PlanningFlow.application.dto.matching.RankedCandidateResponseDto;
import RUT.PlanningFlow.application.dto.matching.RejectedCandidateResponseDto;
import RUT.PlanningFlow.application.dto.matching.ScoreBreakdownResponseDto;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.RequiredSkillCoverage;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.RankedCandidate;
import RUT.PlanningFlow.domain.service.matching.model.RejectedCandidate;
import RUT.PlanningFlow.domain.service.matching.model.ScoreBreakdown;
import RUT.PlanningFlow.domain.vo.EventMode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MatchTaskResponseMapper {

    public MatchTaskResponseDto toResponse(
            final Task task,
            final int requiredCount,
            final EventMode mode,
            final List<RankedCandidate> ranked,
            final List<RejectedCandidate> rejected,
            final int shortageCount,
            final Map<Integer, CandidateSnapshot> snapshots
    ) {
        final List<Skill> requiredSkills = requiredSkills(task);
        final long maxDailyLoadMinutes = durationToMinutes(
                mode == null ? null : mode.workloadPolicy().maxDailyLoad()
        );

        final List<RankedCandidateResponseDto> rankedDtos = new ArrayList<>();
        if (ranked != null) {
            for (final RankedCandidate rc : ranked) {
                if (rc != null) {
                    rankedDtos.add(toRankedDto(rc, task, requiredSkills, maxDailyLoadMinutes, snapshots));
                }
            }
        }

        final List<RejectedCandidateResponseDto> rejectedDtos = new ArrayList<>();
        if (rejected != null) {
            for (final RejectedCandidate rj : rejected) {
                if (rj != null) {
                    rejectedDtos.add(toRejectedDto(rj, task, requiredSkills, maxDailyLoadMinutes, snapshots));
                }
            }
        }

        return new MatchTaskResponseDto(
                task == null ? null : task.getId(),
                requiredCount,
                List.copyOf(rankedDtos),
                List.copyOf(rejectedDtos),
                shortageCount
        );
    }

    private RankedCandidateResponseDto toRankedDto(
            final RankedCandidate rc,
            final Task task,
            final List<Skill> requiredSkills,
            final long maxDailyLoadMinutes,
            final Map<Integer, CandidateSnapshot> snapshots
    ) {
        final User user = rc.candidate();
        final CandidateSnapshot snapshot = snapshotFor(user, snapshots);
        return new RankedCandidateResponseDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                toScoreDto(rc.score()),
                rc.rank(),
                snapshot.geographicDistanceMetersTo(task == null ? null : task.getLocation()),
                durationToMinutes(snapshot.workedToday()),
                maxDailyLoadMinutes,
                RequiredSkillCoverage.userSkillIdsCovering(requiredSkills, snapshot)
        );
    }

    private RejectedCandidateResponseDto toRejectedDto(
            final RejectedCandidate rejected,
            final Task task,
            final List<Skill> requiredSkills,
            final long maxDailyLoadMinutes,
            final Map<Integer, CandidateSnapshot> snapshots
    ) {
        final User user = rejected.candidate();
        final CandidateSnapshot snapshot = snapshotFor(user, snapshots);
        return new RejectedCandidateResponseDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                rejected.reason().name(),
                rejected.details(),
                snapshot.geographicDistanceMetersTo(task == null ? null : task.getLocation()),
                durationToMinutes(snapshot.workedToday()),
                maxDailyLoadMinutes,
                RequiredSkillCoverage.userSkillIdsCovering(requiredSkills, snapshot)
        );
    }

    private static CandidateSnapshot snapshotFor(
            final User user,
            final Map<Integer, CandidateSnapshot> snapshots
    ) {
        if (user == null || user.getId() == null || snapshots == null) {
            return CandidateSnapshot.empty();
        }
        return snapshots.getOrDefault(user.getId(), CandidateSnapshot.empty());
    }

    private static ScoreBreakdownResponseDto toScoreDto(final ScoreBreakdown score) {
        if (score == null) {
            return new ScoreBreakdownResponseDto(0d, 0d, 0d, 0d);
        }
        return new ScoreBreakdownResponseDto(
                score.totalScore(),
                score.skillScore(),
                score.geoScore(),
                score.loadScore()
        );
    }

    private static List<Skill> requiredSkills(final Task task) {
        if (task == null) {
            return List.of();
        }
        final List<Skill> required = task.getRequiredSkills();
        return required == null ? List.of() : List.copyOf(required);
    }

    private static long durationToMinutes(final Duration duration) {
        return duration == null ? 0L : Math.max(0L, duration.toMinutes());
    }
}