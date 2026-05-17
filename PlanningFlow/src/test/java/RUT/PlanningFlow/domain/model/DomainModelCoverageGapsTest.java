package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.BookingStatus;
import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.enums.IncidentSeverity;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.enums.SkillTier;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainModelCoverageGapsTest {

    private static final LocalDateTime T0 = DomainFixtures.EVENT_RANGE_START.plusHours(3);
    private static final LocalDateTime T1 = T0.plusHours(3);

    @Test
    void event_start_planning_requires_coordinator() {
        final Event event = new Event(
                1,
                "E",
                null,
                EventStatus.DRAFT,
                DomainFixtures.EVENT_RANGE_START,
                DomainFixtures.EVENT_RANGE_END,
                null,
                DomainFixtures.user(1),
                List.of()
        );

        assertThatThrownBy(event::startPlanning)
                .hasFieldOrPropertyWithValue("errorCode", "EVENT_COORDINATOR_REQUIRED");
    }

    @Test
    void event_start_planning_and_activate_are_idempotent() {
        final User creator = DomainFixtures.user(1);
        final Event planning = new Event(
                1,
                "E",
                null,
                EventStatus.PLANNING,
                DomainFixtures.EVENT_RANGE_START,
                DomainFixtures.EVENT_RANGE_END,
                null,
                creator,
                List.of(creator)
        );
        planning.startPlanning();
        assertThat(planning.getStatus()).isEqualTo(EventStatus.PLANNING);

        final Event active = new Event(
                2,
                "E2",
                null,
                EventStatus.ACTIVE,
                DomainFixtures.EVENT_RANGE_START,
                DomainFixtures.EVENT_RANGE_END,
                null,
                creator,
                List.of()
        );
        active.activate();
        assertThat(active.getStatus()).isEqualTo(EventStatus.ACTIVE);
    }

    @Test
    void event_cancel_resolves_open_incidents_and_is_idempotent() {
        final User creator = DomainFixtures.user(1);
        final Event event = new Event(
                1,
                "E",
                null,
                EventStatus.ACTIVE,
                DomainFixtures.EVENT_RANGE_START,
                DomainFixtures.EVENT_RANGE_END,
                null,
                creator,
                List.of()
        );
        final Incident incident = new Incident(
                5,
                event,
                null,
                null,
                creator,
                "Проблема",
                IncidentSeverity.LOW,
                IncidentStatus.IN_PROGRESS,
                T0,
                null,
                null
        );

        event.cancel("Погода", List.of(incident));

        assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.RESOLVED);

        event.cancel("again", List.of());
        assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
    }

    @Test
    void event_closed_guards_block_mutations_and_incidents() {
        final User creator = DomainFixtures.user(1);
        final Event completed = new Event(
                1,
                "E",
                null,
                EventStatus.COMPLETED,
                DomainFixtures.EVENT_RANGE_START,
                DomainFixtures.EVENT_RANGE_END,
                null,
                creator,
                List.of()
        );

        assertThatThrownBy(() -> completed.updateInfo("X", "d"))
                .hasFieldOrPropertyWithValue("errorCode", "EVENT_CLOSED");
        assertThatThrownBy(completed::assertAllowsReportingIncidents)
                .hasFieldOrPropertyWithValue("errorCode", "EVENT_CLOSED_FOR_INCIDENTS");
    }

    @Test
    void event_update_dates_rejects_when_event_finished() {
        final User creator = DomainFixtures.user(1);
        final Event event = new Event(
                1,
                "E",
                null,
                EventStatus.CANCELLED,
                DomainFixtures.EVENT_RANGE_START,
                DomainFixtures.EVENT_RANGE_END,
                null,
                creator,
                List.of()
        );

        assertThatThrownBy(() -> event.updateDates(T0, T1, List.of()))
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_EVENT_STATE");
    }

    @Test
    void event_location_latitude_longitude_and_equals() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final GeoPoint point = DomainFixtures.moscowCenter();

        event.updateLocation(point);

        assertThat(event.getLatitude()).isEqualTo(point.getLatitude());
        assertThat(event.getLongitude()).isEqualTo(point.getLongitude());
        assertThat(event).isEqualTo(event);
        assertThat(event.hashCode()).isEqualTo(event.hashCode());
    }

    @Test
    void task_assert_schedule_duration_allowed_static() {
        final LocalDateTime start = T0;
        final LocalDateTime okEnd = start.plusHours(8);

        Task.assertScheduleDurationAllowed(start, okEnd);

        assertThatThrownBy(() -> Task.assertScheduleDurationAllowed(start, start.plusHours(8).plusMinutes(1)))
                .hasFieldOrPropertyWithValue("errorCode", "TASK_DURATION_EXCEEDS_LIMIT");
    }

    @Test
    void task_assert_eligible_for_new_incident_rejects_closed() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task done = DomainFixtures.taskWithStatus(1, event, creator, TaskStatus.DONE, T0, T1);
        final Task cancelled = DomainFixtures.taskWithStatus(2, event, creator, TaskStatus.CANCELLED, T0, T1);

        assertThatThrownBy(done::assertEligibleForNewIncidentAttachment)
                .hasFieldOrPropertyWithValue("errorCode", "TASK_CLOSED_FOR_INCIDENT");
        assertThatThrownBy(cancelled::assertEligibleForNewIncidentAttachment)
                .hasFieldOrPropertyWithValue("errorCode", "TASK_CLOSED_FOR_INCIDENT");
    }

    @Test
    void task_closed_rejects_planner_mutations() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task done = DomainFixtures.taskWithStatus(1, event, creator, TaskStatus.DONE, T0, T1);
        final Task cancelled = DomainFixtures.taskWithStatus(2, event, creator, TaskStatus.CANCELLED, T0, T1);

        assertThatThrownBy(done::assertAllowsPlannerMutations)
                .hasFieldOrPropertyWithValue("errorCode", "TASK_CLOSED_FOR_EDIT");
        assertThatThrownBy(cancelled::assertAllowsPlannerMutations)
                .hasFieldOrPropertyWithValue("errorCode", "TASK_CLOSED_FOR_EDIT");
        assertThatThrownBy(() -> done.rename("Новое название"))
                .hasFieldOrPropertyWithValue("errorCode", "TASK_CLOSED_FOR_EDIT");
    }

    @Test
    void user_rejects_age_over_120_and_supports_equals() {
        final LocalDate tooOld = LocalDate.now().minusYears(121);

        assertThatThrownBy(() -> new User(1, "u", "p", "e@e.com", "N", tooOld, List.of()))
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_BIRTH_DATE");

        final User a = DomainFixtures.user(3);
        final User b = DomainFixtures.user(3);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void incident_update_description() {
        final User creator = DomainFixtures.user(1);
        final Incident incident = new Incident(
                1,
                DomainFixtures.event(1, creator),
                null,
                null,
                creator,
                "old",
                IncidentSeverity.LOW,
                IncidentStatus.OPEN,
                T0,
                null,
                null
        );

        incident.updateDescription("  new text  ");

        assertThat(incident.getDescription()).isEqualTo("  new text  ");
    }

    @Test
    void resource_booking_constructor_validations_and_reschedule_confirmed() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final InternalResource resource = new InternalResource(1, "R", ResourceType.EQUIPMENT, "INV");
        final LocalDateTime rf = T0.plusMinutes(30);
        final LocalDateTime rt = T0.plusHours(2);

        assertThatThrownBy(() -> new ResourceBooking(1, null, resource, BookingStatus.REQUESTED, rf, rt))
                .hasFieldOrPropertyWithValue("errorCode", "TASK_REQUIRED");
        assertThatThrownBy(() -> new ResourceBooking(1, task, null, BookingStatus.REQUESTED, rf, rt))
                .hasFieldOrPropertyWithValue("errorCode", "RESOURCE_REQUIRED");
        assertThatThrownBy(() -> new ResourceBooking(1, task, resource, null, rf, rt))
                .hasFieldOrPropertyWithValue("errorCode", "BOOKING_STATUS_REQUIRED");

        final ResourceBooking confirmed = new ResourceBooking(2, task, resource, BookingStatus.CONFIRMED, rf, rt);
        final LocalDateTime nrf = T0.plusHours(1);
        final LocalDateTime nrt = T0.plusHours(2).plusMinutes(30);

        confirmed.reschedule(nrf, nrt);

        assertThat(confirmed.getReservedFrom()).isEqualTo(nrf);
        assertThat(confirmed.getReservedTo()).isEqualTo(nrt);
    }

    @Test
    void resource_booking_cancel_idempotent_and_equals() {
        final User creator = DomainFixtures.user(1);
        final Task task = DomainFixtures.openTask(10, DomainFixtures.event(1, creator), creator, T0, T1);
        final InternalResource resource = new InternalResource(1, "R", ResourceType.EQUIPMENT, "INV");
        final ResourceBooking booking = new ResourceBooking(
                1,
                task,
                resource,
                BookingStatus.REQUESTED,
                T0.plusMinutes(15),
                T0.plusHours(1)
        );

        booking.cancel();
        booking.cancel();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking).isEqualTo(booking);
    }

    @Test
    void assignment_reject_from_accepted_and_equals() {
        final User creator = DomainFixtures.user(1);
        final Task task = DomainFixtures.openTask(10, DomainFixtures.event(1, creator), creator, T0, T1);
        final Assignment assignment = new Assignment(
                1,
                task,
                DomainFixtures.user(2),
                AssignStatus.ACCEPTED,
                T0.minusHours(1),
                T0,
                null
        );
        final LocalDateTime response = T0.plusMinutes(5);

        assignment.reject(response, "schedule conflict");

        assertThat(assignment.getStatus()).isEqualTo(AssignStatus.REJECTED);
        assertThat(assignment.getRejectionReason()).isEqualTo("schedule conflict");

        final Assignment sameId = new Assignment(1, task, DomainFixtures.user(99), AssignStatus.PENDING, T0, null, null);
        assertThat(assignment).isEqualTo(sameId);
    }

    @Test
    void skill_rename_blank_rejected() {
        final Skill skill = new Skill(1, "Name", "Cat");

        assertThatThrownBy(() -> skill.rename("  "))
                .hasFieldOrPropertyWithValue("errorCode", "SKILL_NAME_REQUIRED");
    }

    @Test
    void role_equals_by_id() {
        final Role a = new Role(1, UserRoles.ADMIN);
        final Role b = new Role(1, UserRoles.ORGANIZER);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void internal_and_external_resource_equals_same_id() {
        final InternalResource internal = new InternalResource(1, "A", ResourceType.EQUIPMENT, "inv");
        final InternalResource same = new InternalResource(1, "B", ResourceType.TRANSPORT, "inv2");

        assertThat(internal).isEqualTo(same);

        final ExternalResource external = new ExternalResource(2, "V", ResourceType.MATERIAL, "api");
        final ExternalResource sameExt = new ExternalResource(2, "W", ResourceType.TRANSPORT, "api2");

        assertThat(external).isEqualTo(sameExt);
    }

    @Test
    void internal_resource_update_inventory_number() {
        final InternalResource resource = new InternalResource(1, "Van", ResourceType.TRANSPORT, "INV-1");

        resource.updateInventoryNumber("INV-2");

        assertThat(resource.getInventoryNumber()).isEqualTo("INV-2");
    }

    @Test
    void resource_constructor_rejects_blank_name() {
        assertThatThrownBy(() -> new InternalResource(1, "  ", ResourceType.EQUIPMENT, "INV"))
                .hasFieldOrPropertyWithValue("errorCode", "RESOURCE_NAME_REQUIRED");
    }

    @Test
    void task_lifecycle_rejects_invalid_transitions() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task open = DomainFixtures.openTask(10, event, creator, T0, T1);
        final Task assigned = DomainFixtures.taskWithStatus(11, event, creator, TaskStatus.ASSIGNED, T0, T1);

        assertThatThrownBy(open::startExecution)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_TASK_STATE");
        assertThatThrownBy(assigned::markAsDone)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_TASK_STATE");

        final Task inProgress = DomainFixtures.taskWithStatus(12, event, creator, TaskStatus.IN_PROGRESS, T0, T1);
        final Assignment assignment = DomainFixtures.pendingAssignment(1, inProgress, DomainFixtures.user(2), T0);
        assertThatThrownBy(() -> inProgress.unassign(assignment))
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_TASK_STATE");
    }

    @Test
    void resource_booking_fail_idempotent_and_confirm_wrong_state() {
        final User creator = DomainFixtures.user(1);
        final Task task = DomainFixtures.openTask(10, DomainFixtures.event(1, creator), creator, T0, T1);
        final InternalResource resource = new InternalResource(1, "R", ResourceType.EQUIPMENT, "INV");
        final LocalDateTime rf = T0.plusMinutes(20);
        final LocalDateTime rt = T0.plusHours(1);
        final ResourceBooking failed = new ResourceBooking(1, task, resource, BookingStatus.FAILED, rf, rt);

        failed.fail();
        assertThat(failed.getStatus()).isEqualTo(BookingStatus.FAILED);

        assertThatThrownBy(() -> new ResourceBooking(2, task, resource, BookingStatus.FAILED, rf, rt).confirm())
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_BOOKING_STATE");
    }

    @Test
    void event_complete_ignores_incident_from_other_event() {
        final User creator = DomainFixtures.user(1);
        final Event event = new Event(
                1,
                "E",
                null,
                EventStatus.ACTIVE,
                DomainFixtures.EVENT_RANGE_START,
                DomainFixtures.EVENT_RANGE_END,
                null,
                creator,
                List.of()
        );
        final Event other = DomainFixtures.event(2, creator);
        final Task done = DomainFixtures.taskWithStatus(10, event, creator, TaskStatus.DONE, T0, T1);
        final Incident foreign = new Incident(
                20,
                other,
                null,
                null,
                creator,
                "x",
                IncidentSeverity.LOW,
                IncidentStatus.OPEN,
                T0,
                null,
                null
        );

        event.complete(List.of(done), List.of(foreign));

        assertThat(foreign.getStatus()).isEqualTo(IncidentStatus.OPEN);
    }

    @Test
    void incident_and_user_skill_equals_by_id() {
        final User creator = DomainFixtures.user(1);
        final Incident a = new Incident(
                3,
                DomainFixtures.event(1, creator),
                null,
                null,
                creator,
                "d",
                IncidentSeverity.LOW,
                IncidentStatus.OPEN,
                T0,
                null,
                null
        );
        final Incident b = new Incident(
                3,
                DomainFixtures.event(2, creator),
                null,
                null,
                creator,
                "other",
                IncidentSeverity.HIGH,
                IncidentStatus.RESOLVED,
                T0,
                T0,
                "n"
        );

        assertThat(a).isEqualTo(b);

        final UserSkill usA = new UserSkill(8, creator, DomainFixtures.skill(1, "S", "C"), SkillTier.NOVICE, null);
        final UserSkill usB = new UserSkill(8, DomainFixtures.user(2), DomainFixtures.skill(2, "T", "D"), SkillTier.EXPERT, T0);

        assertThat(usA).isEqualTo(usB);
    }
}
