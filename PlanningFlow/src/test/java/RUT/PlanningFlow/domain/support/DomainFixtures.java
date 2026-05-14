package RUT.PlanningFlow.domain.support;

import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.model.Assignment;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.vo.EventMode;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import RUT.PlanningFlow.domain.vo.MatchingDistance;
import RUT.PlanningFlow.domain.vo.WorkloadPolicy;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class DomainFixtures {

    public static final LocalDateTime EVENT_RANGE_START = LocalDateTime.of(2026, 6, 1, 6, 0);
    public static final LocalDateTime EVENT_RANGE_END = LocalDateTime.of(2026, 6, 5, 22, 0);

    private DomainFixtures() {
    }

    public static User user(final int id) {
        return new User(
                id,
                "user" + id,
                "password",
                "user" + id + "@example.com",
                "User " + id,
                LocalDate.now().minusYears(20),
                List.of()
        );
    }

    public static Event event(final int id, final User creator) {
        return new Event(
                id,
                "Event " + id,
                "Description",
                EventStatus.PLANNING,
                EVENT_RANGE_START,
                EVENT_RANGE_END,
                null,
                creator,
                List.of()
        );
    }

    public static Task openTask(
            final int id,
            final Event event,
            final User creator,
            final LocalDateTime start,
            final LocalDateTime end
    ) {
        return new Task(
                id,
                event,
                creator,
                "Task " + id,
                TaskStatus.OPEN,
                start,
                end,
                null,
                List.of(),
                List.of()
        );
    }

    public static Task taskWithStatus(
            final int id,
            final Event event,
            final User creator,
            final TaskStatus status,
            final LocalDateTime start,
            final LocalDateTime end
    ) {
        return new Task(
                id,
                event,
                creator,
                "Task " + id,
                status,
                start,
                end,
                null,
                List.of(),
                List.of()
        );
    }

    public static Skill skill(final int id, final String name, final String category) {
        return new Skill(id, name, category);
    }

    public static Assignment pendingAssignment(
            final int id,
            final Task task,
            final User assignee,
            final LocalDateTime assignedAt
    ) {
        return new Assignment(id, task, assignee, AssignStatus.PENDING, assignedAt, null, null);
    }

    public static WorkloadPolicy workloadPolicy(final Duration maxDaily, final Duration minGap) {
        return new WorkloadPolicy(maxDaily, minGap);
    }

    public static EventMode eventMode(
            final MatchingMode matchingMode,
            final MatchingDistance distance,
            final WorkloadPolicy policy
    ) {
        return new EventMode(matchingMode, distance, policy);
    }

    public static EventMode defaultEventMode() {
        return eventMode(MatchingMode.STANDARD, MatchingDistance.BUILDING_SCALE, WorkloadPolicy.defaults());
    }

    public static MatchingContext matchingContext(
            final LocalDateTime now,
            final EventMode eventMode,
            final Map<Integer, CandidateSnapshot> snapshots
    ) {
        return matchingContext(now, eventMode, snapshots, java.util.Set.of());
    }

    public static MatchingContext matchingContext(
            final LocalDateTime now,
            final EventMode eventMode,
            final Map<Integer, CandidateSnapshot> snapshots,
            final java.util.Set<Integer> userIdsWithActiveAssignmentOnTask
    ) {
        return new MatchingContext(now, eventMode, snapshots, userIdsWithActiveAssignmentOnTask);
    }

    public static GeoPoint moscowCenter() {
        return new GeoPoint(55.7558, 37.6173);
    }
}
