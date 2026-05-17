package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskTest {

    private static final LocalDateTime T0 = DomainFixtures.EVENT_RANGE_START.plusHours(2);
    private static final LocalDateTime T1 = T0.plusHours(3);

    @Nested
    @DisplayName("assign / unassign")
    class Assign {

        @Test
        void assign_changes_status_to_assigned() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
            final User volunteer = DomainFixtures.user(2);
            final Assignment assignment = new Assignment(
                    1,
                    task,
                    volunteer,
                    AssignStatus.PENDING,
                    LocalDateTime.now(),
                    null,
                    null
            );

            task.assign(assignment);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.ASSIGNED);
        }

        @Test
        void assign_is_idempotent_when_already_assigned() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
            final User volunteer = DomainFixtures.user(2);
            final Assignment assignment = DomainFixtures.pendingAssignment(1, task, volunteer, LocalDateTime.now());
            task.assign(assignment);

            task.assign(assignment);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.ASSIGNED);
        }

        @Test
        void assign_rejects_mismatched_assignment() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
            final Task other = DomainFixtures.openTask(11, event, creator, T0.plusDays(1), T1.plusDays(1));
            final Assignment foreign = DomainFixtures.pendingAssignment(1, other, DomainFixtures.user(2), LocalDateTime.now());

            assertThatThrownBy(() -> task.assign(foreign))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "ASSIGNMENT_TASK_MISMATCH");
        }

        @Test
        void unassign_returns_task_to_open() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
            final User volunteer = DomainFixtures.user(2);
            final Assignment assignment = DomainFixtures.pendingAssignment(1, task, volunteer, LocalDateTime.now());
            task.assign(assignment);

            task.unassign(assignment);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.OPEN);
        }
    }

    @Nested
    @DisplayName("lifecycle")
    class Lifecycle {

        @Test
        void start_execution_requires_assigned_and_dependencies_done() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final LocalDateTime depEnd = T0.minusMinutes(30);
            final Task dependency = DomainFixtures.taskWithStatus(
                    20,
                    event,
                    creator,
                    TaskStatus.DONE,
                    DomainFixtures.EVENT_RANGE_START.plusHours(1),
                    depEnd
            );
            final Task task = new Task(
                    10,
                    event,
                    creator,
                    "Blocked",
                    TaskStatus.ASSIGNED,
                    T0,
                    T1,
                    null,
                    java.util.List.of(),
                    java.util.List.of(dependency)
            );

            task.startExecution();

            assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        }

        @Test
        void mark_done_from_in_progress() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.taskWithStatus(10, event, creator, TaskStatus.IN_PROGRESS, T0, T1);

            task.markAsDone();

            assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
        }

        @Test
        void cancel_from_open() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);

            task.cancel();

            assertThat(task.getStatus()).isEqualTo(TaskStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("required skills")
    class Skills {

        @Test
        void add_required_skill_deduplicates_by_id() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
            final Skill s = DomainFixtures.skill(5, "First Aid", "Medical");

            task.addRequiredSkill(s);
            task.addRequiredSkill(DomainFixtures.skill(5, "Duplicate name", "Medical"));

            assertThat(task.getRequiredSkills()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("location")
    class Location {

        @Test
        void update_location_sets_geo_point() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
            final GeoPoint point = DomainFixtures.moscowCenter();

            task.updateLocation(point);

            assertThat(task.getLocation()).isEqualTo(point);
        }

        @Test
        void clear_location() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
            task.updateLocation(DomainFixtures.moscowCenter());

            task.clearLocation();

            assertThat(task.getLocation()).isNull();
        }
    }

    @Nested
    @DisplayName("dependencies")
    class Dependencies {

        @Test
        void self_dependency_rejected() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);

            assertThatThrownBy(() -> task.addDependency(task))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "SELF_DEPENDENCY_NOT_ALLOWED");
        }

        @Test
        void cyclic_dependency_rejected() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task a = DomainFixtures.openTask(1, event, creator, T0.plusHours(6), T1.plusHours(6));
            final Task b = DomainFixtures.openTask(2, event, creator, T0.plusHours(3), T1.plusHours(3));
            a.addDependency(b);

            assertThatThrownBy(() -> b.addDependency(a))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "CYCLIC_DEPENDENCY_NOT_ALLOWED");
        }

        @Test
        void remove_dependency_by_id() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task dep = DomainFixtures.taskWithStatus(
                    20,
                    event,
                    creator,
                    TaskStatus.DONE,
                    DomainFixtures.EVENT_RANGE_START.plusMinutes(30),
                    DomainFixtures.EVENT_RANGE_START.plusHours(2)
            );
            final Task task = new Task(
                    10,
                    event,
                    creator,
                    "Main",
                    TaskStatus.OPEN,
                    T0,
                    T1,
                    null,
                    java.util.List.of(),
                    java.util.List.of(dep)
            );

            task.removeDependency(dep);

            assertThat(task.getDependencies()).isEmpty();
        }
    }

    @Nested
    @DisplayName("schedule")
    class Schedule {

        @Test
        void constructor_schedule_outside_event_rejected() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);

            assertThatThrownBy(() -> new Task(
                    10,
                    event,
                    creator,
                    "Early task",
                    TaskStatus.OPEN,
                    DomainFixtures.EVENT_RANGE_START.minusHours(1),
                    DomainFixtures.EVENT_RANGE_START.plusHours(1),
                    null,
                    java.util.List.of(),
                    java.util.List.of()
            ))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "TASK_OUT_OF_EVENT_RANGE");
        }

        @Test
        void move_schedule_outside_event_rejected() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);

            assertThatThrownBy(() -> task.moveSchedule(DomainFixtures.EVENT_RANGE_START.minusHours(1), T0))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "TASK_OUT_OF_EVENT_RANGE");
        }

        @Test
        void schedule_longer_than_eight_hours_rejected_on_move() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
            final LocalDateTime badEnd = T0.plusHours(9);

            assertThatThrownBy(() -> task.moveSchedule(T0, badEnd))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "TASK_DURATION_EXCEEDS_LIMIT");
        }
    }

    @Nested
    @DisplayName("invalid assign")
    class InvalidAssign {

        @Test
        void assign_is_noop_when_task_in_progress() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.taskWithStatus(10, event, creator, TaskStatus.IN_PROGRESS, T0, T1);
            final Assignment assignment = new Assignment(
                    1,
                    task,
                    DomainFixtures.user(2),
                    AssignStatus.PENDING,
                    LocalDateTime.now(),
                    null,
                    null
            );

            task.assign(assignment);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        }

        @Test
        void assign_rejects_when_task_done() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.taskWithStatus(10, event, creator, TaskStatus.DONE, T0, T1);
            final Assignment assignment = new Assignment(
                    1,
                    task,
                    DomainFixtures.user(2),
                    AssignStatus.PENDING,
                    LocalDateTime.now(),
                    null,
                    null
            );

            assertThatThrownBy(() -> task.assign(assignment))
                    .hasFieldOrPropertyWithValue("errorCode", "INVALID_TASK_STATE");
        }

        @Test
        void assign_rejects_when_task_cancelled() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.taskWithStatus(10, event, creator, TaskStatus.CANCELLED, T0, T1);
            final Assignment assignment = new Assignment(
                    1,
                    task,
                    DomainFixtures.user(2),
                    AssignStatus.PENDING,
                    LocalDateTime.now(),
                    null,
                    null
            );

            assertThatThrownBy(() -> task.assign(assignment))
                    .hasFieldOrPropertyWithValue("errorCode", "INVALID_TASK_STATE");
        }
    }

    @Nested
    @DisplayName("cancel")
    class CancelDone {

        @Test
        void cannot_cancel_done_task() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.taskWithStatus(10, event, creator, TaskStatus.DONE, T0, T1);

            assertThatThrownBy(task::cancel)
                    .hasFieldOrPropertyWithValue("errorCode", "INVALID_TASK_STATE");
        }
    }
}
