package RUT.PlanningFlow.domain.service.matching.model;

import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import RUT.PlanningFlow.domain.vo.EventMode;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public record MatchingContext(
        LocalDateTime now,
        EventMode eventMode,
        Map<Integer, CandidateSnapshot> candidateSnapshots,
        
        Set<Integer> userIdsWithActiveAssignmentOnTask
) {
    public MatchingContext {
        DomainAssert.notNull(now, "Текущее время расчета обязательно", "MATCHING_NOW_REQUIRED");
        DomainAssert.notNull(eventMode, "Режим мероприятия обязателен", "EVENT_MODE_REQUIRED");
        candidateSnapshots = candidateSnapshots == null ? Map.of() : Map.copyOf(candidateSnapshots);
        userIdsWithActiveAssignmentOnTask = userIdsWithActiveAssignmentOnTask == null
                ? Set.of()
                : Set.copyOf(userIdsWithActiveAssignmentOnTask);
    }

    public CandidateSnapshot snapshotFor(final User candidate) {
        DomainAssert.notNull(candidate, "Кандидат обязателен", "MATCHING_CANDIDATE_REQUIRED");
        if (candidate.getId() == null) {
            return CandidateSnapshot.empty();
        }
        return candidateSnapshots.getOrDefault(candidate.getId(), CandidateSnapshot.empty());
    }
}
