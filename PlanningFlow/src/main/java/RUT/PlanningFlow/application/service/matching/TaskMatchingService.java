package RUT.PlanningFlow.application.service.matching;

import RUT.PlanningFlow.application.dto.matching.MatchTaskResponseDto;
import RUT.PlanningFlow.application.port.in.matching.MatchTaskUseCase;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Assignment;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.model.UserSkill;
import RUT.PlanningFlow.domain.service.matching.MatchingEngine;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.service.matching.model.MatchingResult;
import RUT.PlanningFlow.domain.service.matching.model.RankedCandidate;
import RUT.PlanningFlow.domain.service.matching.model.RejectedCandidate;
import RUT.PlanningFlow.domain.vo.EventMode;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import RUT.PlanningFlow.domain.vo.ScheduleInterval;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class TaskMatchingService implements MatchTaskUseCase {

    private final TaskRepositoryPort taskRepository;
    private final UserRepositoryPort userRepository;
    private final AssignmentRepositoryPort assignmentRepository;
    private final MatchTaskResponseMapper responseMapper = new MatchTaskResponseMapper();
    private final MatchingEngine matchingEngine = new MatchingEngine();

    public TaskMatchingService(
            final TaskRepositoryPort taskRepository,
            final UserRepositoryPort userRepository,
            final AssignmentRepositoryPort assignmentRepository
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public MatchTaskResponseDto execute(
            final Integer callerUserId,
            final Integer taskId,
            final EventMode mode,
            final int requiredCount
    ) {
        if (callerUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DomainException("Задача не найдена", "TASK_NOT_FOUND"));
        PlanningAccessPolicy.assertCanManageTaskAsPlanner(actor, task);
        final Set<Integer> userIdsAlreadyOnTask = userIdsWithActiveAssignmentOnTask(taskId);
        final List<User> candidates = userRepository.findActiveParticipant();

        final LocalDateTime matchingInstant = LocalDateTime.now();
        final LocalDate workloadDay = task.getStartTime().toLocalDate();
        final SnapshotData snapshotData = buildSnapshotData(
                candidates,
                workloadDay,
                matchingInstant,
                task.getId()
        );
        final Map<Integer, CandidateSnapshot> snapshots = snapshotData.snapshots();

        final MatchingContext standardContext = new MatchingContext(
                matchingInstant,
                mode,
                snapshots,
                userIdsAlreadyOnTask
        );

        final MatchingResult standard = matchingEngine.match(task, candidates, requiredCount, standardContext);
        if (!standard.hasShortage() || mode.matchingMode() == MatchingMode.CRITICAL) {
            final List<RankedCandidate> ranked = assignSequentialRanks(standard.rankedCandidates());
            return responseMapper.toResponse(
                    task,
                    requiredCount,
                    mode,
                    ranked,
                    rejectedExcludingAssigned(standard.rejectedCandidates(), ranked),
                    standard.shortageCount(),
                    snapshots
            );
        }

        final EventMode criticalMode = new EventMode(
                MatchingMode.CRITICAL,
                mode.maxGeographicDistance(),
                mode.workloadPolicy()
        );
        final MatchingContext criticalContext = new MatchingContext(
                matchingInstant,
                criticalMode,
                snapshots,
                userIdsAlreadyOnTask
        );
        final MatchingResult critical = matchingEngine.match(task, candidates, requiredCount, criticalContext);

        final List<RankedCandidate> mergedRanked = assignSequentialRanks(
                mergeRanked(standard.rankedCandidates(), critical.rankedCandidates(), requiredCount)
        );
        final int mergedShortage = Math.max(0, requiredCount - mergedRanked.size());

        return responseMapper.toResponse(
                task,
                requiredCount,
                mode,
                mergedRanked,
                rejectedExcludingAssigned(standard.rejectedCandidates(), mergedRanked),
                mergedShortage,
                snapshots
        );
    }

    private record SnapshotData(Map<Integer, CandidateSnapshot> snapshots) {
    }

    private Set<Integer> userIdsWithActiveAssignmentOnTask(final Integer taskId) {
        final List<Assignment> onTask = assignmentRepository.findByTaskId(taskId);
        return onTask.stream()
                .filter(a -> a != null && a.getUser() != null && a.getUser().getId() != null)
                .filter(a -> a.getStatus() == AssignStatus.PENDING || a.getStatus() == AssignStatus.ACCEPTED)
                .map(a -> a.getUser().getId())
                .collect(Collectors.toSet());
    }

    private static List<RejectedCandidate> rejectedExcludingAssigned(
            final List<RejectedCandidate> rejected,
            final List<RankedCandidate> ranked
    ) {
        final Set<Integer> assignedIds = new HashSet<>();
        for (final RankedCandidate rc : ranked) {
            if (rc != null && rc.candidate() != null && rc.candidate().getId() != null) {
                assignedIds.add(rc.candidate().getId());
            }
        }
        return rejected.stream()
                .filter(rj -> rj != null && rj.candidate() != null && rj.candidate().getId() != null
                        && !assignedIds.contains(rj.candidate().getId()))
                .toList();
    }

    private static List<RankedCandidate> mergeRanked(
            final List<RankedCandidate> standard,
            final List<RankedCandidate> critical,
            final int requiredCount
    ) {
        final Map<Integer, RankedCandidate> unique = new LinkedHashMap<>();
        for (final RankedCandidate rc : standard) {
            if (rc == null || rc.candidate() == null || rc.candidate().getId() == null) {
                continue;
            }
            unique.putIfAbsent(rc.candidate().getId(), rc);
            if (unique.size() >= requiredCount) {
                return List.copyOf(unique.values());
            }
        }
        for (final RankedCandidate rc : critical) {
            if (rc == null || rc.candidate() == null || rc.candidate().getId() == null) {
                continue;
            }
            unique.putIfAbsent(rc.candidate().getId(), rc);
            if (unique.size() >= requiredCount) {
                break;
            }
        }
        return List.copyOf(unique.values());
    }

    private static List<RankedCandidate> assignSequentialRanks(final List<RankedCandidate> ordered) {
        if (ordered == null || ordered.isEmpty()) {
            return List.of();
        }
        final List<RankedCandidate> out = new ArrayList<>(ordered.size());
        for (int i = 0; i < ordered.size(); i++) {
            final RankedCandidate rc = ordered.get(i);
            out.add(new RankedCandidate(rc.candidate(), rc.score(), i + 1));
        }
        return List.copyOf(out);
    }

    private SnapshotData buildSnapshotData(
            final List<User> candidates,
            final LocalDate workloadDay,
            final LocalDateTime matchingInstant,
            final Integer excludeTaskId
    ) {
        final Map<Integer, CandidateSnapshot> snapshots = new HashMap<>();
        for (final User candidate : candidates) {
            final Integer userId = candidate.getId();
            if (userId == null) {
                continue;
            }
            final List<UserSkill> skills = safeList(userRepository.findSkillsForUser(userId));

            final List<Task> userTasksOnDay = safeList(taskRepository.findCommittedTasksForUserOnDate(userId, workloadDay));
            final CandidateTemporalSnapshot temporal = computeTemporalSnapshot(
                    userTasksOnDay,
                    workloadDay,
                    matchingInstant,
                    excludeTaskId
            );
            final CandidateSnapshot snapshot = CandidateSnapshot.create(
                    temporal.occupiedUntil(),
                    temporal.committedIntervals(),
                    temporal.previousTaskEndedAt(),
                    temporal.previousTaskLocation(),
                    temporal.previousTaskDuration(),
                    temporal.workedToday(),
                    skills
            );
            snapshots.put(userId, snapshot);
        }
        return new SnapshotData(Map.copyOf(snapshots));
    }

    private static CandidateTemporalSnapshot computeTemporalSnapshot(
            final List<Task> tasks,
            final LocalDate workloadDay,
            final LocalDateTime now,
            final Integer excludeTaskId
    ) {
        LocalDateTime occupiedUntil = null;
        Task previousTask = null;
        Duration workedToday = Duration.ZERO;
        final List<ScheduleInterval> committedIntervals = new ArrayList<>();

        for (final Task task : tasks) {
            if (task == null || excludeTaskId != null && excludeTaskId.equals(task.getId())) {
                continue;
            }

            final LocalDateTime start = task.getStartTime();
            final LocalDateTime end = task.getEndTime();
            if (start == null || end == null) {
                continue;
            }

            committedIntervals.add(new ScheduleInterval(start, end));

            if (start.toLocalDate().equals(workloadDay)) {
                final Duration duration = Duration.between(start, end);
                if (!duration.isNegative()) {
                    workedToday = workedToday.plus(duration);
                }
            }

            if (!now.isBefore(start) && now.isBefore(end)) {
                if (occupiedUntil == null || end.isAfter(occupiedUntil)) {
                    occupiedUntil = end;
                }
            }

            if (end.toLocalDate().equals(workloadDay) && !end.isAfter(now) && task.getStatus() == TaskStatus.DONE) {
                if (previousTask == null || end.isAfter(previousTask.getEndTime())) {
                    previousTask = task;
                }
            }
        }

        final LocalDateTime previousTaskEndedAt = previousTask == null ? null : previousTask.getEndTime();
        return new CandidateTemporalSnapshot(
                occupiedUntil,
                List.copyOf(committedIntervals),
                previousTaskEndedAt,
                previousTask == null ? null : previousTask.getLocation(),
                previousTask == null ? Duration.ZERO : safeDuration(previousTask.getStartTime(), previousTask.getEndTime()),
                workedToday
        );
    }

    private static Duration safeDuration(final LocalDateTime start, final LocalDateTime end) {
        if (start == null || end == null) {
            return Duration.ZERO;
        }
        final Duration d = Duration.between(start, end);
        return d.isNegative() ? Duration.ZERO : d;
    }

    private static <T> List<T> safeList(final List<T> value) {
        return value == null ? List.of() : value;
    }

    private record CandidateTemporalSnapshot(
            LocalDateTime occupiedUntil,
            List<ScheduleInterval> committedIntervals,
            LocalDateTime previousTaskEndedAt,
            GeoPoint previousTaskLocation,
            Duration previousTaskDuration,
            Duration workedToday
    ) {
    }
}