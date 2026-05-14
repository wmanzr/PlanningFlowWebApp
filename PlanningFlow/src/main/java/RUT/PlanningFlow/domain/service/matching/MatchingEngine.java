package RUT.PlanningFlow.domain.service.matching;

import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.filter.CandidatePhaseAFilter;
import RUT.PlanningFlow.domain.service.matching.filter.CandidatePhaseBFilter;
import RUT.PlanningFlow.domain.service.matching.filter.HardConstraintsCandidateFilter;
import RUT.PlanningFlow.domain.service.matching.filter.WeightedScoreCandidateFilter;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.service.matching.model.MatchingResult;
import RUT.PlanningFlow.domain.service.matching.model.RankedCandidate;
import RUT.PlanningFlow.domain.service.matching.model.RejectedCandidate;
import RUT.PlanningFlow.domain.service.matching.model.ScoreBreakdown;
import RUT.PlanningFlow.domain.service.matching.selection.DefaultSelection;
import RUT.PlanningFlow.domain.service.matching.selection.FinalSelection;
import RUT.PlanningFlow.domain.utils.DomainAssert;

import java.util.ArrayList;
import java.util.List;

public final class MatchingEngine {

    private final CandidatePhaseAFilter phaseAFilter;
    private final CandidatePhaseBFilter phaseBFilter;
    private final FinalSelection finalSelection;

    public MatchingEngine() {
        this(
                new HardConstraintsCandidateFilter(),
                WeightedScoreCandidateFilter.balancedDefaults(),
                new DefaultSelection()
        );
    }

    public MatchingEngine(
            final CandidatePhaseAFilter phaseAFilter,
            final CandidatePhaseBFilter phaseBFilter,
            final FinalSelection finalSelection
    ) {
        DomainAssert.notNull(phaseAFilter, "Фильтр фазы A обязателен", "PHASE_A_FILTER_REQUIRED");
        DomainAssert.notNull(phaseBFilter, "Фильтр фазы B обязателен", "PHASE_B_FILTER_REQUIRED");
        DomainAssert.notNull(finalSelection, "Политика финального выбора обязательна", "FINAL_SELECTION_REQUIRED");
        this.phaseAFilter = phaseAFilter;
        this.phaseBFilter = phaseBFilter;
        this.finalSelection = finalSelection;
    }

    public MatchingResult match(final Task task, final List<User> candidates, final int requiredCount, final MatchingContext context) {
        DomainAssert.notNull(task, "Задача для подбора обязательна", "MATCHING_TASK_REQUIRED");
        DomainAssert.notNull(candidates, "Список кандидатов обязателен", "MATCHING_CANDIDATES_REQUIRED");
        DomainAssert.notNull(context, "Контекст подбора обязателен", "MATCHING_CONTEXT_REQUIRED");
        DomainAssert.isTrue(requiredCount > 0, "Количество требуемых кандидатов должно быть положительным", "INVALID_REQUIRED_COUNT");
        assertTaskEligibleForMatching(task);

        final Integer taskId = task.getId();

        if (candidates.isEmpty()) {
            return new MatchingResult(taskId, requiredCount, List.of(), List.of(), requiredCount);
        }

        final List<RejectedCandidate> rejectedCandidates = new ArrayList<>();
        final List<User> passedPhaseA = runPhaseA(task, candidates, context, rejectedCandidates);
        if (passedPhaseA.isEmpty()) {
            return new MatchingResult(taskId, requiredCount, List.of(), rejectedCandidates, requiredCount);
        }

        final List<RankedCandidate> phaseBRanked = runPhaseB(task, passedPhaseA, context);
        final PhaseCResult phaseCResult = runPhaseC(phaseBRanked, requiredCount);
        return new MatchingResult(
                taskId,
                requiredCount,
                phaseCResult.rankedWithPositions(),
                rejectedCandidates,
                phaseCResult.shortageCount()
        );
    }

    private List<User> runPhaseA(
            final Task task,
            final List<User> candidates,
            final MatchingContext context,
            final List<RejectedCandidate> rejectedCandidates
    ) {
        final List<User> passed = new ArrayList<>();
        for (User candidate : candidates) {
            final RejectedCandidate rejection = phaseAFilter.rejectOrNull(task, candidate, context);
            if (rejection != null) {
                rejectedCandidates.add(rejection);
            } else {
                passed.add(candidate);
            }
        }
        return passed;
    }

    private List<RankedCandidate> runPhaseB(
            final Task task,
            final List<User> passedPhaseA,
            final MatchingContext context
    ) {
        final List<RankedCandidate> rankedCandidates = new ArrayList<>();
        for (User candidate : passedPhaseA) {
            final ScoreBreakdown score = phaseBFilter.score(task, candidate, context);
            rankedCandidates.add(new RankedCandidate(candidate, score, 0));
        }
        return rankedCandidates;
    }

    private PhaseCResult runPhaseC(
            final List<RankedCandidate> phaseBRanked,
            final int requiredCount
    ) {
        final List<RankedCandidate> ordered = finalSelection.select(phaseBRanked);

        final List<RankedCandidate> rankedWithPosition = new ArrayList<>(ordered.size());
        for (int i = 0; i < ordered.size(); i++) {
            final RankedCandidate raw = ordered.get(i);
            rankedWithPosition.add(new RankedCandidate(raw.candidate(), raw.score(), i + 1));
        }

        final int poolSize = rankedWithPosition.size();
        final int shortageCount = Math.max(0, requiredCount - poolSize);

        return new PhaseCResult(List.copyOf(rankedWithPosition), shortageCount);
    }

    private record PhaseCResult(
            List<RankedCandidate> rankedWithPositions,
            int shortageCount
    ) {
    }

    private static void assertTaskEligibleForMatching(final Task task) {
        final TaskStatus status = task.getStatus();
        DomainAssert.isTrue(
                status != TaskStatus.DONE && status != TaskStatus.CANCELLED,
                "Подбор недоступен для завершенной или отмененной задачи",
                "MATCHING_TASK_CLOSED"
        );
    }
}