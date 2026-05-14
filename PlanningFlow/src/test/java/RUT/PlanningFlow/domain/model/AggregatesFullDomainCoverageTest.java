package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.BookingStatus;
import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.enums.IncidentSeverity;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AggregatesFullDomainCoverageTest {

    private static final LocalDateTime T0 = DomainFixtures.EVENT_RANGE_START.plusHours(3);
    private static final LocalDateTime T1 = T0.plusHours(3);

    @Test
    void assignment_constructor_validates() {
        final Task task = DomainFixtures.openTask(1, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);
        final User user = DomainFixtures.user(2);

        assertThatThrownBy(() -> new Assignment(1, null, user, AssignStatus.PENDING, LocalDateTime.now(), null, null))
                .hasFieldOrPropertyWithValue("errorCode", "TASK_REQUIRED");
        assertThatThrownBy(() -> new Assignment(1, task, null, AssignStatus.PENDING, LocalDateTime.now(), null, null))
                .hasFieldOrPropertyWithValue("errorCode", "USER_REQUIRED");
        assertThatThrownBy(() -> new Assignment(1, task, user, null, LocalDateTime.now(), null, null))
                .hasFieldOrPropertyWithValue("errorCode", "ASSIGNMENT_STATUS_REQUIRED");
        assertThatThrownBy(() -> new Assignment(1, task, user, AssignStatus.PENDING, null, null, null))
                .hasFieldOrPropertyWithValue("errorCode", "ASSIGNED_AT_REQUIRED");
    }

    @Test
    void assignment_accept_rejects_non_pending() {
        final Task task = DomainFixtures.openTask(1, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);
        final Assignment cancelled = new Assignment(1, task, DomainFixtures.user(2), AssignStatus.CANCELLED, LocalDateTime.now(), null, null);

        assertThatThrownBy(() -> cancelled.accept(LocalDateTime.now()))
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_ASSIGNMENT_STATE");
    }

    @Test
    void assignment_accept_requires_response_time() {
        final Assignment a = DomainFixtures.pendingAssignment(
                1,
                DomainFixtures.openTask(1, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1),
                DomainFixtures.user(2),
                LocalDateTime.now()
        );

        assertThatThrownBy(() -> a.accept(null))
                .hasFieldOrPropertyWithValue("errorCode", "RESPONSE_TIME_REQUIRED");
    }

    @Test
    void assignment_reject_from_accepted() {
        final User creator = DomainFixtures.user(1);
        final Task task = DomainFixtures.openTask(1, DomainFixtures.event(1, creator), creator, T0, T1);
        final Assignment a = new Assignment(1, task, DomainFixtures.user(2), AssignStatus.ACCEPTED, LocalDateTime.now(), LocalDateTime.now(), null);

        a.reject(LocalDateTime.now(), "no capacity");

        assertThat(a.getStatus()).isEqualTo(AssignStatus.REJECTED);
    }

    @Test
    void assignment_reject_idempotent_when_already_rejected() {
        final Assignment a = new Assignment(
                1,
                DomainFixtures.openTask(1, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1),
                DomainFixtures.user(2),
                AssignStatus.REJECTED,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "x"
        );

        a.reject(LocalDateTime.now(), "again");

        assertThat(a.getStatus()).isEqualTo(AssignStatus.REJECTED);
    }

    @Test
    void assignment_cancel_coordinator_idempotent() {
        final Assignment a = new Assignment(
                1,
                DomainFixtures.openTask(1, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1),
                DomainFixtures.user(2),
                AssignStatus.CANCELLED,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        a.cancelByCoordinator(LocalDateTime.now());

        assertThat(a.getStatus()).isEqualTo(AssignStatus.CANCELLED);
    }

    @Test
    void assignment_cancel_coordinator_null_response_rejected() {
        final Assignment a = DomainFixtures.pendingAssignment(
                1,
                DomainFixtures.openTask(1, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1),
                DomainFixtures.user(2),
                LocalDateTime.now()
        );

        assertThatThrownBy(() -> a.cancelByCoordinator(null))
                .hasFieldOrPropertyWithValue("errorCode", "RESPONSE_TIME_REQUIRED");
    }

    @Test
    void assignment_equals_by_id() {
        final Task task = DomainFixtures.openTask(1, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);
        final Assignment a = new Assignment(7, task, DomainFixtures.user(2), AssignStatus.PENDING, LocalDateTime.now(), null, null);
        final Assignment b = new Assignment(7, task, DomainFixtures.user(3), AssignStatus.ACCEPTED, LocalDateTime.now(), null, null);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void incident_constructor_validations() {
        final Event event = DomainFixtures.event(1, DomainFixtures.user(1));

        assertThatThrownBy(() -> new Incident(
                1,
                null,
                null,
                null,
                DomainFixtures.user(1),
                "d",
                IncidentSeverity.LOW,
                IncidentStatus.OPEN,
                LocalDateTime.now(),
                null,
                null
        )).hasFieldOrPropertyWithValue("errorCode", "EVENT_REQUIRED");

        assertThatThrownBy(() -> new Incident(
                1,
                event,
                null,
                null,
                null,
                "d",
                IncidentSeverity.LOW,
                IncidentStatus.OPEN,
                LocalDateTime.now(),
                null,
                null
        )).hasFieldOrPropertyWithValue("errorCode", "INCIDENT_REPORTER_REQUIRED");

        assertThatThrownBy(() -> new Incident(
                1,
                event,
                null,
                null,
                DomainFixtures.user(1),
                " ",
                IncidentSeverity.LOW,
                IncidentStatus.OPEN,
                LocalDateTime.now(),
                null,
                null
        )).hasFieldOrPropertyWithValue("errorCode", "INCIDENT_DESCRIPTION_REQUIRED");

        assertThatThrownBy(() -> new Incident(
                1,
                event,
                null,
                null,
                DomainFixtures.user(1),
                "ok",
                null,
                IncidentStatus.OPEN,
                LocalDateTime.now(),
                null,
                null
        )).hasFieldOrPropertyWithValue("errorCode", "INCIDENT_SEVERITY_REQUIRED");

        assertThatThrownBy(() -> new Incident(
                1,
                event,
                null,
                null,
                DomainFixtures.user(1),
                "ok",
                IncidentSeverity.LOW,
                null,
                LocalDateTime.now(),
                null,
                null
        )).hasFieldOrPropertyWithValue("errorCode", "INCIDENT_STATUS_REQUIRED");

        assertThatThrownBy(() -> new Incident(
                1,
                event,
                null,
                null,
                DomainFixtures.user(1),
                "ok",
                IncidentSeverity.LOW,
                IncidentStatus.OPEN,
                null,
                null,
                null
        )).hasFieldOrPropertyWithValue("errorCode", "INCIDENT_CREATED_AT_REQUIRED");
    }

    @Test
    void incident_mark_in_progress_wrong_state() {
        final Incident incident = new Incident(
                1,
                DomainFixtures.event(1, DomainFixtures.user(1)),
                null,
                null,
                DomainFixtures.user(1),
                "x",
                IncidentSeverity.LOW,
                IncidentStatus.RESOLVED,
                LocalDateTime.now(),
                null,
                null
        );

        assertThatThrownBy(incident::markAsInProgress)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_INCIDENT_STATE");
    }

    @Test
    void incident_resolve_idempotent() {
        final Incident incident = new Incident(
                1,
                DomainFixtures.event(1, DomainFixtures.user(1)),
                null,
                null,
                DomainFixtures.user(1),
                "x",
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
    void incident_change_severity_null_rejected() {
        final Incident incident = new Incident(
                1,
                DomainFixtures.event(1, DomainFixtures.user(1)),
                null,
                null,
                DomainFixtures.user(1),
                "x",
                IncidentSeverity.LOW,
                IncidentStatus.OPEN,
                LocalDateTime.now(),
                null,
                null
        );

        assertThatThrownBy(() -> incident.changeSeverity(null))
                .hasFieldOrPropertyWithValue("errorCode", "INCIDENT_SEVERITY_REQUIRED");
    }

    @Test
    void resource_booking_constructor_and_confirm_fail_cancel_paths() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final InternalResource resource = new InternalResource(1, "R", ResourceType.EQUIPMENT, "INV-1");
        final LocalDateTime rf = T0.plusMinutes(30);
        final LocalDateTime rt = T0.plusHours(2);

        assertThatThrownBy(() -> new ResourceBooking(1, null, resource, BookingStatus.REQUESTED, rf, rt))
                .hasFieldOrPropertyWithValue("errorCode", "TASK_REQUIRED");

        final ResourceBooking booking = new ResourceBooking(1, task, resource, BookingStatus.REQUESTED, rf, rt);

        booking.confirm();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        booking.confirm();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        assertThatThrownBy(() -> new ResourceBooking(9, task, resource, BookingStatus.FAILED, rf, rt).confirm())
                .isInstanceOf(DomainException.class);

        final ResourceBooking failed = new ResourceBooking(2, task, resource, BookingStatus.FAILED, rf, rt);
        failed.fail();

        final ResourceBooking cancelled = new ResourceBooking(3, task, resource, BookingStatus.REQUESTED, rf, rt);
        cancelled.cancel();

        assertThatThrownBy(() -> new ResourceBooking(4, task, resource, BookingStatus.CONFIRMED, rf, rt).fail())
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_BOOKING_STATE");
    }

    @Test
    void resource_booking_reschedule_rejects_inactive_status() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final InternalResource resource = new InternalResource(1, "R", ResourceType.EQUIPMENT, "INV-1");
        final LocalDateTime rf = T0.plusMinutes(30);
        final LocalDateTime rt = T0.plusHours(2);
        final ResourceBooking cancelled = new ResourceBooking(1, task, resource, BookingStatus.CANCELLED, rf, rt);

        assertThatThrownBy(() -> cancelled.reschedule(rf.plusHours(1), rt.plusHours(1)))
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_BOOKING_STATE");
    }

    @Test
    void resource_rename_blank_and_change_type_null() {
        final InternalResource resource = new InternalResource(1, "X", ResourceType.EQUIPMENT, "INV");

        assertThatThrownBy(() -> resource.rename(" "))
                .hasFieldOrPropertyWithValue("errorCode", "RESOURCE_NAME_REQUIRED");

        assertThatThrownBy(() -> resource.changeType(null))
                .hasFieldOrPropertyWithValue("errorCode", "RESOURCE_TYPE_REQUIRED");
    }

    @Test
    void resource_mark_operational_idempotent() {
        final InternalResource resource = new InternalResource(1, "X", ResourceType.EQUIPMENT, "INV");

        resource.markOperational();

        assertThat(resource.isOperational()).isTrue();
    }

    @Test
    void resource_equals_requires_same_concrete_type() {
        final InternalResource i = new InternalResource(1, "A", ResourceType.EQUIPMENT, "x");
        final ExternalResource e = new ExternalResource(1, "B", ResourceType.TRANSPORT, "api");

        assertThat(i).isNotEqualTo(e);
    }

    @Test
    void user_constructor_validations_and_role_null() {
        assertThatThrownBy(() -> new User(1, "", "p", "e@e.com", "N", java.time.LocalDate.now().minusYears(20), List.of()))
                .hasFieldOrPropertyWithValue("errorCode", "USERNAME_REQUIRED");

        final User user = DomainFixtures.user(1);

        assertThatThrownBy(() -> user.addRole(null))
                .hasFieldOrPropertyWithValue("errorCode", "ROLE_REQUIRED");

        assertThatThrownBy(() -> user.removeRole(null))
                .hasFieldOrPropertyWithValue("errorCode", "ROLE_REQUIRED");
    }

    @Test
    void user_rename_email_password_blank_rejected() {
        final User user = DomainFixtures.user(1);

        assertThatThrownBy(() -> user.rename(" "))
                .hasFieldOrPropertyWithValue("errorCode", "FULL_NAME_REQUIRED");
        assertThatThrownBy(() -> user.changeEmail(" "))
                .hasFieldOrPropertyWithValue("errorCode", "EMAIL_REQUIRED");
        assertThatThrownBy(() -> user.changePassword(" "))
                .hasFieldOrPropertyWithValue("errorCode", "PASSWORD_REQUIRED");
    }

    @Test
    void event_start_planning_wrong_state() {
        final Event event = new Event(
                1,
                "T",
                null,
                EventStatus.ACTIVE,
                DomainFixtures.EVENT_RANGE_START,
                DomainFixtures.EVENT_RANGE_END,
                null,
                DomainFixtures.user(1),
                List.of()
        );

        assertThatThrownBy(event::startPlanning)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_EVENT_STATE");
    }

    @Test
    void event_activate_wrong_state() {
        final Event event = new Event(
                1,
                "T",
                null,
                EventStatus.DRAFT,
                DomainFixtures.EVENT_RANGE_START,
                DomainFixtures.EVENT_RANGE_END,
                null,
                DomainFixtures.user(1),
                List.of()
        );

        assertThatThrownBy(event::activate)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_EVENT_STATE");
    }

    @Test
    void event_complete_idempotent_when_already_completed() {
        final Event event = new Event(
                1,
                "T",
                null,
                EventStatus.COMPLETED,
                DomainFixtures.EVENT_RANGE_START,
                DomainFixtures.EVENT_RANGE_END,
                null,
                DomainFixtures.user(1),
                List.of()
        );

        event.complete(List.of());

        assertThat(event.getStatus()).isEqualTo(EventStatus.COMPLETED);
    }

    @Test
    void event_validate_task_attach_null_task() {
        assertThatThrownBy(() -> DomainFixtures.event(1, DomainFixtures.user(1)).validateTaskAttach(null))
                .hasFieldOrPropertyWithValue("errorCode", "TASK_REQUIRED");
    }

    @Test
    void event_remove_coordinator() {
        final User creator = DomainFixtures.user(1);
        final User coord = DomainFixtures.user(5);
        final Event event = DomainFixtures.event(1, creator);
        event.addCoordinator(coord);

        event.removeCoordinator(coord);

        assertThat(event.getCoordinators()).isEmpty();
    }

    @Test
    void event_update_dates_rejects_task_outside_new_window() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(9, event, creator, T0, T1);
        final LocalDateTime narrowStart = T0.plusHours(1);
        final LocalDateTime narrowEnd = T0.plusHours(2);

        assertThatThrownBy(() -> event.updateDates(narrowStart, narrowEnd, List.of(task)))
                .hasFieldOrPropertyWithValue("errorCode", "TASK_OUT_OF_EVENT_RANGE");
    }

    @Test
    void skill_constructor_blank_name() {
        assertThatThrownBy(() -> new Skill(1, "  ", "Cat"))
                .hasFieldOrPropertyWithValue("errorCode", "SKILL_NAME_REQUIRED");
    }
}
