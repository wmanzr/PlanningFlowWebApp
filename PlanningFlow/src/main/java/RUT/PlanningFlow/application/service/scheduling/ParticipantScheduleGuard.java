package RUT.PlanningFlow.application.service.scheduling;

import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Assignment;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.service.scheduling.ScheduleConflictPolicy;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import RUT.PlanningFlow.domain.vo.ScheduleInterval;
import RUT.PlanningFlow.domain.vo.WorkloadPolicy;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ParticipantScheduleGuard {

    private final TaskRepositoryPort taskRepository;
    private final AssignmentRepositoryPort assignmentRepository;

    public ParticipantScheduleGuard(
            final TaskRepositoryPort taskRepository,
            final AssignmentRepositoryPort assignmentRepository
    ) {
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(assignmentRepository, "Репозиторий назначений обязателен", "ASSIGNMENT_REPOSITORY_REQUIRED");
        this.taskRepository = taskRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public void assertNoScheduleConflict(
            final Integer userId,
            final LocalDateTime start,
            final LocalDateTime end,
            final Integer excludeTaskId,
            final Duration minTechnicalGap
    ) {
        DomainAssert.notNull(userId, "ID участника обязателен", "USER_ID_REQUIRED");
        if (start == null || end == null) {
            return;
        }
        final List<ScheduleInterval> committed = committedIntervalsForUserOnDate(userId, start.toLocalDate(), excludeTaskId);
        if (ScheduleConflictPolicy.conflictsWithCommitted(start, end, committed, minTechnicalGap)) {
            throw new DomainException("Конфликт расписания с другим назначением", "SCHEDULE_CONFLICT");
        }
    }

    public void assertNotAlreadyAssignedOnTask(final Integer taskId, final Integer userId) {
        DomainAssert.notNull(taskId, "ID задачи обязателен", "TASK_ID_REQUIRED");
        DomainAssert.notNull(userId, "ID участника обязателен", "USER_ID_REQUIRED");
        final List<Assignment> onTask = assignmentRepository.findByTaskId(taskId);
        final boolean alreadyAssigned = onTask.stream()
                .filter(a -> a != null && a.getUser() != null && userId.equals(a.getUser().getId()))
                .anyMatch(a -> a.getStatus() == AssignStatus.PENDING || a.getStatus() == AssignStatus.ACCEPTED);
        if (alreadyAssigned) {
            throw new DomainException("Участник уже назначен на эту задачу", "ASSIGNMENT_ALREADY_EXISTS");
        }
    }

    public List<ScheduleInterval> committedIntervalsForUserOnDate(
            final Integer userId,
            final LocalDate day,
            final Integer excludeTaskId
    ) {
        final List<Task> tasks = taskRepository.findCommittedTasksForUserOnDate(userId, day);
        return toIntervals(tasks, excludeTaskId);
    }

    public static Duration defaultMinTechnicalGap() {
        return WorkloadPolicy.defaults().minTechnicalGap();
    }

    private static List<ScheduleInterval> toIntervals(final List<Task> tasks, final Integer excludeTaskId) {
        if (tasks == null || tasks.isEmpty()) {
            return List.of();
        }
        final List<ScheduleInterval> intervals = new ArrayList<>(tasks.size());
        for (final Task task : tasks) {
            if (task == null || excludeTaskId != null && excludeTaskId.equals(task.getId())) {
                continue;
            }
            final LocalDateTime start = task.getStartTime();
            final LocalDateTime end = task.getEndTime();
            if (start == null || end == null) {
                continue;
            }
            intervals.add(new ScheduleInterval(start, end));
        }
        return List.copyOf(intervals);
    }
}
