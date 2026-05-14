package RUT.PlanningFlow.domain.service.matching.filter;

import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.service.matching.model.RejectedCandidate;
import RUT.PlanningFlow.domain.service.matching.model.RejectionReason;
import RUT.PlanningFlow.domain.service.scheduling.ScheduleConflictPolicy;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import RUT.PlanningFlow.domain.vo.WorkloadPolicy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public final class HardConstraintsCandidateFilter implements CandidatePhaseAFilter {

    @Override
    public RejectedCandidate rejectOrNull(
            final Task task,
            final User candidate,
            final MatchingContext context
    ) {
        final Integer candidateId = candidate.getId();
        if (candidateId != null && context.userIdsWithActiveAssignmentOnTask().contains(candidateId)) {
            return new RejectedCandidate(
                    candidate,
                    RejectionReason.TIME_CONFLICT,
                    "Уже назначен на эту задачу"
            );
        }

        final CandidateSnapshot snapshot = context.snapshotFor(candidate);
        if (ScheduleConflictPolicy.conflictsWithCommitted(
                task.getStartTime(),
                task.getEndTime(),
                snapshot.committedIntervals(),
                context.eventMode().workloadPolicy().minTechnicalGap()
        )) {
            return new RejectedCandidate(candidate, RejectionReason.TIME_CONFLICT, "Конфликт расписания с другим назначением");
        }

        final RejectedCandidate skillsRejection = evaluateRequiredSkills(task, candidate, snapshot);
        if (skillsRejection != null) {
            return skillsRejection;
        }

        final Duration taskDuration = estimateTaskDuration(task);
        if (!withinDailyLoad(snapshot, taskDuration, context.eventMode().workloadPolicy())) {
            return new RejectedCandidate(candidate, RejectionReason.DAILY_LOAD_EXCEEDED, "Превышен суточный лимит нагрузки");
        }

        return null;
    }

    private static RejectedCandidate evaluateRequiredSkills(final Task task, final User candidate, final CandidateSnapshot snapshot) {
        if (task.getRequiredSkills().isEmpty()) {
            return null;
        }

        final Set<String> requiredCategories = task.getRequiredSkills().stream()
                .map(CandidateSnapshot::normalizedCategory)
                .collect(Collectors.toSet());

        final Set<String> possessedCategories = snapshot.cumulativeCategoryWeights().keySet();

        final boolean everyCategoryRepresented = possessedCategories.containsAll(requiredCategories);

        if (!everyCategoryRepresented) {
            return new RejectedCandidate(
                    candidate,
                    RejectionReason.MISSING_REQUIRED_SKILLS,
                    "У кандидата нет ни одного навыка в категории, требуемой для задачи"
            );
        }
        return null;
    }

    private static Duration estimateTaskDuration(final Task task) {
        final Duration duration = Duration.between(task.getStartTime(), task.getEndTime());
        return duration.isNegative() ? Duration.ZERO : duration;
    }

    private static boolean withinDailyLoad(
            final CandidateSnapshot snapshot,
            final Duration additionalTaskDuration,
            final WorkloadPolicy workloadPolicy
    ) {
        DomainAssert.notNull(workloadPolicy, "Политика нагрузки обязательна", "WORKLOAD_POLICY_REQUIRED");
        DomainAssert.notNull(additionalTaskDuration, "Длительность задачи обязательна", "ADDITIONAL_TASK_DURATION_REQUIRED");
        DomainAssert.isTrue(!additionalTaskDuration.isNegative(), "Длительность задачи не может быть отрицательной", "INVALID_ADDITIONAL_DURATION");
        final Duration worked = snapshot.workedToday() == null ? Duration.ZERO : snapshot.workedToday();
        final Duration total = worked.plus(additionalTaskDuration);
        return total.compareTo(workloadPolicy.maxDailyLoad()) <= 0;
    }
}