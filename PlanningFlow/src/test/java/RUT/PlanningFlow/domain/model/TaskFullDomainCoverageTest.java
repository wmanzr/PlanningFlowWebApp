package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskFullDomainCoverageTest {

    private static final LocalDateTime T0 = DomainFixtures.EVENT_RANGE_START.plusHours(4);
    private static final LocalDateTime T1 = T0.plusHours(3);

    @Test
    void constructor_requires_event() {
        assertThatThrownBy(() -> new Task(
                1,
                null,
                DomainFixtures.user(1),
                "t",
                TaskStatus.OPEN,
                T0,
                T1,
                null,
                List.of(),
                List.of()
        ))
                .hasFieldOrPropertyWithValue("errorCode", "EVENT_REQUIRED");
    }

    @Test
    void constructor_requires_creator_with_id() {
        final User noId = new User(null, "u", "p", "e@e.com", "N", java.time.LocalDate.now().minusYears(20), List.of());
        final Event event = DomainFixtures.event(1, DomainFixtures.user(99));

        assertThatThrownBy(() -> new Task(
                1,
                event,
                noId,
                "t",
                TaskStatus.OPEN,
                T0,
                T1,
                null,
                List.of(),
                List.of()
        ))
                .hasFieldOrPropertyWithValue("errorCode", "TASK_CREATOR_ID_REQUIRED");
    }

    @Test
    void constructor_requires_non_blank_title() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);

        assertThatThrownBy(() -> new Task(
                1,
                event,
                creator,
                " \t",
                TaskStatus.OPEN,
                T0,
                T1,
                null,
                List.of(),
                List.of()
        ))
                .hasFieldOrPropertyWithValue("errorCode", "TASK_TITLE_REQUIRED");
    }

    @Test
    void constructor_requires_status() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);

        assertThatThrownBy(() -> new Task(
                1,
                event,
                creator,
                "t",
                null,
                T0,
                T1,
                null,
                List.of(),
                List.of()
        ))
                .hasFieldOrPropertyWithValue("errorCode", "TASK_STATUS_REQUIRED");
    }

    @Test
    void rename_rejects_blank_title() {
        final Task task = DomainFixtures.openTask(1, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);

        assertThatThrownBy(() -> task.rename(" "))
                .hasFieldOrPropertyWithValue("errorCode", "TASK_TITLE_REQUIRED");
    }

    @Test
    void move_schedule_within_event_succeeds() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final LocalDateTime ns = T0.plusMinutes(30);
        final LocalDateTime ne = T1.minusMinutes(30);

        task.moveSchedule(ns, ne);

        assertThat(task.getStartTime()).isEqualTo(ns);
        assertThat(task.getEndTime()).isEqualTo(ne);
    }

    @Test
    void add_dependency_rejects_different_event() {
        final User creator = DomainFixtures.user(1);
        final Event e1 = DomainFixtures.event(1, creator);
        final Event e2 = DomainFixtures.event(2, creator);
        final LocalDateTime d0 = DomainFixtures.EVENT_RANGE_START.plusHours(1);
        final LocalDateTime d1 = d0.plusHours(2);
        final Task dep = DomainFixtures.openTask(20, e2, creator, d0, d1);
        final Task main = DomainFixtures.openTask(10, e1, creator, d1.plusHours(1), d1.plusHours(4));

        assertThatThrownBy(() -> main.addDependency(dep))
                .hasFieldOrPropertyWithValue("errorCode", "DEPENDENCY_WRONG_EVENT");
    }

    @Test
    void add_dependency_rejects_when_dependency_ends_at_or_after_task_end() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task dep = DomainFixtures.openTask(20, event, creator, T0, T1);
        final Task main = DomainFixtures.openTask(10, event, creator, T1, T1);

        assertThatThrownBy(() -> main.addDependency(dep))
                .hasFieldOrPropertyWithValue("errorCode", "DEPENDENCY_END_NOT_BEFORE_TASK_END");
    }

    @Test
    void add_dependency_schedule_conflict_when_starts_before_dependency_ends() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final LocalDateTime depEnd = T0.plusHours(2);
        final Task dep = DomainFixtures.openTask(20, event, creator, T0.minusHours(2), depEnd);
        final Task main = DomainFixtures.openTask(10, event, creator, T0, T1);

        assertThatThrownBy(() -> main.addDependency(dep))
                .hasFieldOrPropertyWithValue("errorCode", "DEPENDENCY_SCHEDULE_CONFLICT");
    }

    @Test
    void add_dependency_idempotent_for_same_dependency() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task dep = DomainFixtures.taskWithStatus(
                20,
                event,
                creator,
                TaskStatus.DONE,
                DomainFixtures.EVENT_RANGE_START.plusMinutes(30),
                T0.minusMinutes(30)
        );
        final Task main = DomainFixtures.openTask(10, event, creator, T0, T1);

        main.addDependency(dep);
        main.addDependency(dep);

        assertThat(main.getDependencies()).hasSize(1);
    }

    @Test
    void deep_cycle_dependency_detected() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final LocalDateTime cStart = T0;
        final LocalDateTime cEnd = T0.plusHours(1);
        final LocalDateTime bStart = cEnd;
        final LocalDateTime bEnd = bStart.plusHours(1);
        final LocalDateTime aStart = bEnd;
        final LocalDateTime aEnd = aStart.plusHours(1);
        final Task c = DomainFixtures.openTask(3, event, creator, cStart, cEnd);
        final Task b = new Task(2, event, creator, "B", TaskStatus.OPEN, bStart, bEnd, null, List.of(), List.of(c));
        final Task a = new Task(1, event, creator, "A", TaskStatus.OPEN, aStart, aEnd, null, List.of(), List.of(b));

        a.addDependency(b);
        b.addDependency(c);

        assertThatThrownBy(() -> c.addDependency(a))
                .hasFieldOrPropertyWithValue("errorCode", "CYCLIC_DEPENDENCY_NOT_ALLOWED");
    }

    @Test
    void start_execution_is_idempotent_when_already_in_progress() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.taskWithStatus(10, event, creator, TaskStatus.IN_PROGRESS, T0, T1);

        task.startExecution();

        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void start_execution_fails_when_dependency_not_done() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task dep = DomainFixtures.taskWithStatus(
                20,
                event,
                creator,
                TaskStatus.IN_PROGRESS,
                DomainFixtures.EVENT_RANGE_START.plusMinutes(30),
                T0.minusMinutes(30)
        );
        final Task task = new Task(
                10,
                event,
                creator,
                "Main",
                TaskStatus.ASSIGNED,
                T0,
                T1,
                null,
                List.of(),
                List.of(dep)
        );

        assertThatThrownBy(task::startExecution)
                .hasFieldOrPropertyWithValue("errorCode", "DEPENDENCIES_NOT_DONE");
    }

    @Test
    void start_execution_skips_null_dependency_slot() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task dep = DomainFixtures.taskWithStatus(
                20,
                event,
                creator,
                TaskStatus.DONE,
                DomainFixtures.EVENT_RANGE_START.plusMinutes(30),
                T0.minusMinutes(30)
        );
        final Task task = new Task(
                10,
                event,
                creator,
                "Main",
                TaskStatus.ASSIGNED,
                T0,
                T1,
                null,
                List.of(),
                Arrays.asList(dep, null)
        );

        task.startExecution();

        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void mark_done_is_idempotent_when_already_done() {
        final Task task = DomainFixtures.taskWithStatus(
                10,
                DomainFixtures.event(1, DomainFixtures.user(1)),
                DomainFixtures.user(1),
                TaskStatus.DONE,
                T0,
                T1
        );

        task.markAsDone();

        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void cancel_is_idempotent_when_already_cancelled() {
        final Task task = DomainFixtures.taskWithStatus(
                10,
                DomainFixtures.event(1, DomainFixtures.user(1)),
                DomainFixtures.user(1),
                TaskStatus.CANCELLED,
                T0,
                T1
        );

        task.cancel();

        assertThat(task.getStatus()).isEqualTo(TaskStatus.CANCELLED);
    }

    @Test
    void unassign_is_noop_when_already_open() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final Assignment assignment = DomainFixtures.pendingAssignment(1, task, DomainFixtures.user(2), LocalDateTime.now());

        task.unassign(assignment);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.OPEN);
    }

    @Test
    void unassign_rejects_foreign_assignment() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final Task other = DomainFixtures.openTask(11, event, creator, T0.plusDays(1), T1.plusDays(1));
        final Assignment foreign = DomainFixtures.pendingAssignment(1, other, DomainFixtures.user(2), LocalDateTime.now());

        assertThatThrownBy(() -> task.unassign(foreign))
                .hasFieldOrPropertyWithValue("errorCode", "ASSIGNMENT_TASK_MISMATCH");
    }

    @Test
    void add_required_skill_validates_null_and_id() {
        final Task task = DomainFixtures.openTask(10, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);

        assertThatThrownBy(() -> task.addRequiredSkill(null))
                .hasFieldOrPropertyWithValue("errorCode", "REQUIRED_SKILL_REQUIRED");

        assertThatThrownBy(() -> task.addRequiredSkill(new Skill(null, "x", "Medical")))
                .hasFieldOrPropertyWithValue("errorCode", "REQUIRED_SKILL_ID_REQUIRED");
    }

    @Test
    void remove_required_skill_validates_and_removes() {
        final Task task = DomainFixtures.openTask(10, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);
        final Skill skill = DomainFixtures.skill(5, "S", "C");
        task.addRequiredSkill(skill);

        task.removeRequiredSkill(skill);

        assertThat(task.getRequiredSkills()).isEmpty();

        assertThatThrownBy(() -> task.removeRequiredSkill(null))
                .hasFieldOrPropertyWithValue("errorCode", "REQUIRED_SKILL_REQUIRED");
    }

    @Test
    void latitude_longitude_delegate_to_location() {
        final Task task = DomainFixtures.openTask(10, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);
        task.updateLocation(new GeoPoint(10.0, 20.0));

        assertThat(task.getLatitude()).isEqualTo(10.0);
        assertThat(task.getLongitude()).isEqualTo(20.0);

        task.clearLocation();

        assertThat(task.getLatitude()).isNull();
        assertThat(task.getLongitude()).isNull();
    }

    @Test
    void assign_null_assignment_rejected() {
        final Task task = DomainFixtures.openTask(10, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);

        assertThatThrownBy(() -> task.assign(null))
                .hasFieldOrPropertyWithValue("errorCode", "ASSIGNMENT_REQUIRED");
    }

    @Test
    void equals_and_hash_code_require_matching_ids() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task a = DomainFixtures.openTask(5, event, creator, T0, T1);
        final Task b = DomainFixtures.openTask(5, event, creator, T0, T1);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(DomainFixtures.openTask(9, event, creator, T0, T1));
    }

    @Test
    void add_dependency_null_checks() {
        final Task task = DomainFixtures.openTask(10, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);

        assertThatThrownBy(() -> task.addDependency(null))
                .hasFieldOrPropertyWithValue("errorCode", "DEPENDENCY_REQUIRED");

        final Task noId = new Task(
                null,
                DomainFixtures.event(1, DomainFixtures.user(1)),
                DomainFixtures.user(1),
                "x",
                TaskStatus.OPEN,
                DomainFixtures.EVENT_RANGE_START.plusHours(1),
                DomainFixtures.EVENT_RANGE_START.plusHours(2),
                null,
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> task.addDependency(noId))
                .hasFieldOrPropertyWithValue("errorCode", "DEPENDENCY_ID_REQUIRED");
    }

    @Test
    void remove_dependency_null_dependency_rejected() {
        final Task task = DomainFixtures.openTask(10, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);

        assertThatThrownBy(() -> task.removeDependency(null))
                .hasFieldOrPropertyWithValue("errorCode", "DEPENDENCY_REQUIRED");
    }
}
