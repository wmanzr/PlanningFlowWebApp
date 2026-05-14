package RUT.PlanningFlow.domain.service.matching.model;

import RUT.PlanningFlow.domain.utils.DomainAssert;
import java.util.List;

public final class MatchingResult {

    private final Integer taskId;
    private final int requiredCount;
    private final List<RankedCandidate> rankedCandidates;
    private final List<RejectedCandidate> rejectedCandidates;
    private final int shortageCount;

    public MatchingResult(
            final Integer taskId,
            final int requiredCount,
            final List<RankedCandidate> rankedCandidates,
            final List<RejectedCandidate> rejectedCandidates,
            final int shortageCount
    ) {
        DomainAssert.isTrue(requiredCount >= 0, "Требуемое число участников не может быть отрицательным", "INVALID_REQUIRED_COUNT");
        this.taskId = taskId;
        this.requiredCount = requiredCount;
        this.rankedCandidates = rankedCandidates == null ? List.of() : List.copyOf(rankedCandidates);
        this.rejectedCandidates = rejectedCandidates == null ? List.of() : List.copyOf(rejectedCandidates);
        this.shortageCount = Math.max(0, shortageCount);
    }

    public static MatchingResult empty() {
        return new MatchingResult(null, 0, List.of(), List.of(), 0);
    }

    public Integer taskId() {
        return taskId;
    }

    public int requiredCount() {
        return requiredCount;
    }

    public List<RankedCandidate> rankedCandidates() {
        return rankedCandidates;
    }

    public List<RejectedCandidate> rejectedCandidates() {
        return rejectedCandidates;
    }

    public int shortageCount() {
        return shortageCount;
    }

    public boolean hasShortage() {
        return shortageCount > 0;
    }

    public int rankedCount() {
        return rankedCandidates.size();
    }

    public int rejectedCount() {
        return rejectedCandidates.size();
    }
}