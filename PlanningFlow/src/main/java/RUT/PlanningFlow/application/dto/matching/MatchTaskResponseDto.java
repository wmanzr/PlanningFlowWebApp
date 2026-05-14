package RUT.PlanningFlow.application.dto.matching;

import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.RankedCandidate;
import RUT.PlanningFlow.domain.service.matching.model.RejectedCandidate;
import RUT.PlanningFlow.domain.vo.EventMode;
import RUT.PlanningFlow.domain.vo.GeoPoint;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static MatchTaskResponseDto from(
            final Task task,
            final int requiredCount,
            final EventMode mode,
            final List<RankedCandidate> ranked,
            final List<RejectedCandidate> rejected,
            final int shortageCount,
            final Map<Integer, CandidateSnapshot> snapshots
    ) {
        final Set<Integer> requiredSkillIds = requiredSkillIds(task);
        final long maxDailyLoadMinutes = Projection.toMinutes(mode.workloadPolicy().maxDailyLoad());

        final List<RankedCandidateResponseDto> rankedDtos = new ArrayList<>(ranked.size());
        for (final RankedCandidate rc : ranked) {
            if (rc == null) {
                continue;
            }
            rankedDtos.add(RankedCandidateResponseDto.from(rc, task, requiredSkillIds, maxDailyLoadMinutes, snapshots));
        }

        final List<RejectedCandidateResponseDto> rejectedDtos = new ArrayList<>(rejected.size());
        for (final RejectedCandidate rj : rejected) {
            rejectedDtos.add(RejectedCandidateResponseDto.from(rj, task, requiredSkillIds, maxDailyLoadMinutes, snapshots));
        }

        return new MatchTaskResponseDto(task == null ? null : task.getId(), requiredCount, rankedDtos, rejectedDtos, shortageCount);
    }

    private static Set<Integer> requiredSkillIds(final Task task) {
        if (task == null) {
            return Set.of();
        }
        final List<Skill> required = task.getRequiredSkills();
        if (required == null || required.isEmpty()) {
            return Set.of();
        }
        final Set<Integer> ids = new LinkedHashSet<>();
        for (final Skill skill : required) {
            if (skill == null) {
                continue;
            }
            final Integer id = skill.getId();
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    static final class Projection {
        private Projection() {
        }

        static CandidateSnapshot snapshotFor(final User user, final Map<Integer, CandidateSnapshot> snapshots) {
            if (user == null || user.getId() == null || snapshots == null) {
                return CandidateSnapshot.empty();
            }
            return snapshots.getOrDefault(user.getId(), CandidateSnapshot.empty());
        }

        static Double distanceMetersOrNull(final CandidateSnapshot snapshot, final Task task) {
            if (snapshot == null || task == null) {
                return null;
            }
            final GeoPoint from = snapshot.previousTaskLocation();
            final GeoPoint to = task.getLocation();
            if (from == null || to == null) {
                return null;
            }
            return from.haversineDistanceMetersTo(to);
        }

        static List<Integer> matchedRequiredSkillIds(final Set<Integer> requiredSkillIds, final CandidateSnapshot snapshot) {
            if (requiredSkillIds == null || requiredSkillIds.isEmpty() || snapshot == null) {
                return List.of();
            }
            final List<Integer> matched = new ArrayList<>();
            for (final Integer requiredId : requiredSkillIds) {
                if (requiredId != null && snapshot.exactSkillWeights().containsKey(requiredId)) {
                    matched.add(requiredId);
                }
            }
            return matched;
        }

        static long toMinutes(final Duration d) {
            return d == null ? 0L : Math.max(0L, d.toMinutes());
        }
    }
}