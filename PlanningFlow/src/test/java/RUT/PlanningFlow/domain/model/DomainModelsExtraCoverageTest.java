package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.BookingStatus;
import RUT.PlanningFlow.domain.enums.IncidentSeverity;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.enums.SkillTier;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import RUT.PlanningFlow.domain.vo.DateTimeRange;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class DomainModelsExtraCoverageTest {

    private static final LocalDateTime T0 = DomainFixtures.EVENT_RANGE_START.plusHours(4);
    private static final LocalDateTime T1 = T0.plusHours(3);

    @Test
    void incident_resolve_is_idempotent_when_already_resolved() {
        final Incident incident = new Incident(
                1,
                DomainFixtures.event(1, DomainFixtures.user(1)),
                null,
                null,
                DomainFixtures.user(1),
                "d",
                IncidentSeverity.LOW,
                IncidentStatus.RESOLVED,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "done"
        );

        incident.resolve("again");

        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.RESOLVED);
    }

    @Test
    void incident_mark_in_progress_is_idempotent() {
        final Incident incident = new Incident(
                2,
                DomainFixtures.event(1, DomainFixtures.user(1)),
                null,
                null,
                DomainFixtures.user(1),
                "d",
                IncidentSeverity.LOW,
                IncidentStatus.IN_PROGRESS,
                LocalDateTime.now(),
                null,
                null
        );

        incident.markAsInProgress();

        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.IN_PROGRESS);
    }

    @Test
    void incident_equals_reflexive_and_rejects_foreign_types() {
        final Incident incident = new Incident(
                5,
                DomainFixtures.event(1, DomainFixtures.user(1)),
                null,
                null,
                DomainFixtures.user(1),
                "d",
                IncidentSeverity.LOW,
                IncidentStatus.OPEN,
                LocalDateTime.now(),
                null,
                null
        );

        assertThat(incident.equals(incident)).isTrue();
        assertThat(incident.equals(null)).isFalse();
        assertThat(incident.equals("not-incident")).isFalse();
    }

    @Test
    void resource_booking_ctor_rejects_window_before_event() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final InternalResource resource = new InternalResource(1, "R", ResourceType.EQUIPMENT, "INV");

        assertThatThrownBy(() -> new ResourceBooking(
                1,
                task,
                resource,
                BookingStatus.REQUESTED,
                DomainFixtures.EVENT_RANGE_START.minusHours(2),
                DomainFixtures.EVENT_RANGE_START.plusHours(1)
        )).hasFieldOrPropertyWithValue("errorCode", "BOOKING_OUT_OF_EVENT_WINDOW");
    }

    @Test
    void resource_booking_reschedule_rejects_window_outside_event() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final InternalResource resource = new InternalResource(1, "R", ResourceType.EQUIPMENT, "INV");
        final LocalDateTime rf = T0.plusMinutes(15);
        final LocalDateTime rt = T0.plusHours(2);
        final ResourceBooking booking = new ResourceBooking(2, task, resource, BookingStatus.REQUESTED, rf, rt);

        assertThatThrownBy(() -> booking.reschedule(
                DomainFixtures.EVENT_RANGE_END.plusHours(1),
                DomainFixtures.EVENT_RANGE_END.plusHours(3)
        )).hasFieldOrPropertyWithValue("errorCode", "BOOKING_OUT_OF_EVENT_WINDOW");
    }

    @Test
    void resource_booking_fail_and_cancel_are_idempotent_in_terminal_states() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final InternalResource resource = new InternalResource(1, "R", ResourceType.EQUIPMENT, "INV");
        final LocalDateTime rf = T0.plusMinutes(15);
        final LocalDateTime rt = T0.plusHours(2);

        final ResourceBooking failed = new ResourceBooking(3, task, resource, BookingStatus.FAILED, rf, rt);
        failed.fail();
        assertThat(failed.getStatus()).isEqualTo(BookingStatus.FAILED);

        final ResourceBooking cancelled = new ResourceBooking(4, task, resource, BookingStatus.CANCELLED, rf, rt);
        cancelled.cancel();
        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void resource_booking_equals_reflexive() {
        final User creator = DomainFixtures.user(1);
        final Task task = DomainFixtures.openTask(10, DomainFixtures.event(1, creator), creator, T0, T1);
        final InternalResource resource = new InternalResource(1, "R", ResourceType.EQUIPMENT, "INV");
        final ResourceBooking b = new ResourceBooking(9, task, resource, BookingStatus.REQUESTED, T0.plusMinutes(5), T0.plusHours(1));

        assertThat(b.equals(b)).isTrue();
        assertThat(b.equals(null)).isFalse();
    }

    @Test
    void task_start_execution_skips_null_dependency_entries() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final LocalDateTime depEnd = T0;
        final LocalDateTime depStart = depEnd.minusHours(2);
        final Task dependencyDone = DomainFixtures.taskWithStatus(
                50,
                event,
                creator,
                TaskStatus.DONE,
                depStart,
                depEnd
        );
        final Task main = new Task(
                51,
                event,
                creator,
                "Blocked",
                TaskStatus.ASSIGNED,
                T0,
                T1,
                null,
                List.of(),
                Arrays.asList(dependencyDone, null)
        );
        main.startExecution();

        assertThat(main.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void task_cancel_is_idempotent_when_cancelled() {
        final Task task = DomainFixtures.taskWithStatus(
                60,
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
    void task_cancel_rejects_done_status() {
        final Task task = DomainFixtures.taskWithStatus(
                61,
                DomainFixtures.event(1, DomainFixtures.user(1)),
                DomainFixtures.user(1),
                TaskStatus.DONE,
                T0,
                T1
        );

        assertThatThrownBy(task::cancel).hasFieldOrPropertyWithValue("errorCode", "INVALID_TASK_STATE");
    }

    @Test
    void task_equals_reflexive() {
        final Task task = DomainFixtures.openTask(70, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);

        assertThat(task.equals(task)).isTrue();
        assertThat(task.equals(null)).isFalse();
    }

    @Test
    void date_time_range_overlaps_requires_argument() {
        final DateTimeRange a = new DateTimeRange(T0, T1);

        assertThatThrownBy(() -> a.overlaps(null)).hasFieldOrPropertyWithValue("errorCode", "DATE_RANGE_REQUIRED");
    }

    @Test
    void date_time_range_overlaps_disjoint_returns_false() {
        final DateTimeRange a = new DateTimeRange(T0, T1);
        final DateTimeRange b = new DateTimeRange(T1.plusHours(1), T1.plusHours(10));

        assertThat(a.overlaps(b)).isFalse();
    }

    @Test
    void date_time_range_contains_endpoints() {
        final DateTimeRange range = new DateTimeRange(T0, T1);

        assertThat(range.contains(T0)).isTrue();
        assertThat(range.contains(T1)).isTrue();
    }

    @Test
    void user_future_birth_date_rejected() {
        assertThatThrownBy(() -> new User(1, "u", "p", "e@e.com", "N", LocalDate.now().plusDays(1), List.of()))
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_BIRTH_DATE");
    }

    @Test
    void internal_resource_mark_broken_and_operational_are_idempotent() {
        final InternalResource resource = new InternalResource(1, "R", ResourceType.EQUIPMENT, "INV");

        resource.markBroken();
        resource.markBroken();
        assertThat(resource.isOperational()).isFalse();

        resource.markOperational();
        resource.markOperational();
        assertThat(resource.isOperational()).isTrue();
    }

    @Test
    void internal_resource_update_inventory_blank_rejected() {
        final InternalResource resource = new InternalResource(1, "R", ResourceType.EQUIPMENT, "INV");

        assertThatThrownBy(() -> resource.updateInventoryNumber("   "))
                .hasFieldOrPropertyWithValue("errorCode", "INVENTORY_NUMBER_REQUIRED");
    }

    @Test
    void external_resource_update_api_id_blank_rejected() {
        final ExternalResource resource = new ExternalResource(1, "R", ResourceType.EQUIPMENT, "api-1");

        assertThatThrownBy(() -> resource.updateExternalApiId(" "))
                .hasFieldOrPropertyWithValue("errorCode", "EXTERNAL_API_ID_REQUIRED");
    }

    @Test
    void user_skill_update_tier_same_value_is_no_op_for_verified_at() {
        final User user = DomainFixtures.user(1);
        final Skill skill = DomainFixtures.skill(1, "s", "c");
        final LocalDateTime verified = LocalDateTime.of(2025, 3, 1, 12, 0);
        final UserSkill userSkill = new UserSkill(1, user, skill, SkillTier.NOVICE, verified);

        userSkill.updateTier(SkillTier.NOVICE);

        assertThat(userSkill.getVerifiedAt()).isEqualTo(verified);
    }

    @Test
    void role_equals_rejects_null_and_foreign_type() {
        final Role role = new Role(1, UserRoles.ADMIN);

        assertThat(role.equals(null)).isFalse();
        assertThat(role.equals("role")).isFalse();
    }

    @Test
    void assignment_reject_is_idempotent_when_already_rejected() {
        final Task task = DomainFixtures.openTask(1, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);
        final Assignment assignment = new Assignment(1, task, DomainFixtures.user(2), AssignStatus.REJECTED, T0.minusHours(1), T0.minusHours(2), "no");

        assignment.reject(T0, "again");

        assertThat(assignment.getStatus()).isEqualTo(AssignStatus.REJECTED);
    }

    @Test
    void assignment_accept_is_idempotent_when_already_accepted() {
        final Task task = DomainFixtures.openTask(1, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);
        final Assignment assignment = new Assignment(2, task, DomainFixtures.user(2), AssignStatus.ACCEPTED, T0.minusHours(1), T0.minusHours(2), null);

        assignment.accept(T0);

        assertThat(assignment.getStatus()).isEqualTo(AssignStatus.ACCEPTED);
    }

    @Test
    void assignment_cancel_by_coordinator_rejects_when_already_rejected() {
        final Task task = DomainFixtures.openTask(1, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);
        final Assignment assignment = new Assignment(3, task, DomainFixtures.user(2), AssignStatus.REJECTED, T0.minusHours(1), T0.minusHours(2), "no");

        assertThatThrownBy(() -> assignment.cancelByCoordinator(T0))
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_ASSIGNMENT_STATE");
    }

}
