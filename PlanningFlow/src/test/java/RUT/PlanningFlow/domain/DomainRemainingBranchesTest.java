package RUT.PlanningFlow.domain;

import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.model.Assignment;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Incident;
import RUT.PlanningFlow.domain.model.InternalResource;
import RUT.PlanningFlow.domain.model.ResourceBooking;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.BookingStatus;
import RUT.PlanningFlow.domain.enums.IncidentSeverity;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import RUT.PlanningFlow.domain.vo.DateTimeRange;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainRemainingBranchesTest {

    private static final LocalDateTime T0 = DomainFixtures.EVENT_RANGE_START.plusHours(4);
    private static final LocalDateTime T1 = T0.plusHours(3);

    @Test
    void event_start_planning_and_activate_idempotent() {
        final User creator = DomainFixtures.user(1);
        final Event planning = new Event(
                1,
                "T",
                null,
                EventStatus.PLANNING,
                DomainFixtures.EVENT_RANGE_START,
                DomainFixtures.EVENT_RANGE_END,
                null,
                creator,
                List.of()
        );

        planning.startPlanning();

        assertThat(planning.getStatus()).isEqualTo(EventStatus.PLANNING);

        final Event active = new Event(
                2,
                "T",
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
    void event_cancel_idempotent_when_cancelled() {
        final Event event = new Event(
                1,
                "T",
                null,
                EventStatus.CANCELLED,
                DomainFixtures.EVENT_RANGE_START,
                DomainFixtures.EVENT_RANGE_END,
                null,
                DomainFixtures.user(1),
                List.of()
        );

        event.cancel("again");

        assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
    }

    @Test
    void event_start_planning_from_completed_rejected() {
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

        assertThatThrownBy(event::startPlanning)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_EVENT_STATE");
    }

    @Test
    void event_add_coordinator_requires_user_id() {
        final Event event = DomainFixtures.event(1, DomainFixtures.user(1));
        final User noId = new User(null, "u", "p", "e@e.com", "N", java.time.LocalDate.now().minusYears(20), List.of());

        assertThatThrownBy(() -> event.addCoordinator(noId))
                .hasFieldOrPropertyWithValue("errorCode", "EVENT_COORDINATOR_ID_REQUIRED");
    }

    @Test
    void event_progress_all_done_is_hundred() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task done = DomainFixtures.taskWithStatus(1, event, creator, TaskStatus.DONE, T0, T1);

        assertThat(event.calculateProgress(List.of(done))).isEqualTo(100.0);
    }

    @Test
    void event_lat_lon_through_location() {
        final Event event = DomainFixtures.event(1, DomainFixtures.user(1));
        event.updateLocation(DomainFixtures.moscowCenter());

        assertThat(event.getLatitude()).isNotNull();
        assertThat(event.getLongitude()).isNotNull();

        event.clearLocation();

        assertThat(event.getLatitude()).isNull();
    }

    @Test
    void task_mark_done_from_open_rejected() {
        final Task task = DomainFixtures.openTask(10, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);

        assertThatThrownBy(task::markAsDone)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_TASK_STATE");
    }

    @Test
    void task_start_execution_from_open_rejected() {
        final Task task = DomainFixtures.openTask(10, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);

        assertThatThrownBy(task::startExecution)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_TASK_STATE");
    }

    @Test
    void geo_point_equals_and_hash_code() {
        final GeoPoint a = new GeoPoint(10.0, 20.0);
        final GeoPoint b = new GeoPoint(10.0, 20.0);

        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(new GeoPoint(11.0, 20.0));
    }

    @Test
    void date_time_range_equals() {
        final LocalDateTime s = LocalDateTime.of(2026, 2, 1, 10, 0);
        final LocalDateTime e = LocalDateTime.of(2026, 2, 1, 11, 0);
        final DateTimeRange r1 = new DateTimeRange(s, e);
        final DateTimeRange r2 = new DateTimeRange(s, e);

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    void incident_equals_by_id() {
        final Incident a = new Incident(
                4,
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
        final Incident b = new Incident(
                4,
                DomainFixtures.event(2, DomainFixtures.user(1)),
                null,
                null,
                DomainFixtures.user(2),
                "other",
                IncidentSeverity.HIGH,
                IncidentStatus.IN_PROGRESS,
                LocalDateTime.now(),
                null,
                null
        );

        assertThat(a).isEqualTo(b);
    }

    @Test
    void resource_booking_equals_and_accessor_window() {
        final User creator = DomainFixtures.user(1);
        final Task task = DomainFixtures.openTask(10, DomainFixtures.event(1, creator), creator, T0, T1);
        final InternalResource resource = new InternalResource(1, "R", ResourceType.EQUIPMENT, "INV");
        final LocalDateTime rf = T0.plusMinutes(15);
        final LocalDateTime rt = T0.plusHours(2);
        final ResourceBooking x = new ResourceBooking(8, task, resource, BookingStatus.REQUESTED, rf, rt);
        final ResourceBooking y = new ResourceBooking(8, task, resource, BookingStatus.CONFIRMED, rf, rt);

        assertThat(x).isEqualTo(y);
        assertThat(x.getReservationWindow().getStart()).isEqualTo(rf);
    }

    @Test
    void assignment_reject_non_active_states() {
        final Task task = DomainFixtures.openTask(1, DomainFixtures.event(1, DomainFixtures.user(1)), DomainFixtures.user(1), T0, T1);
        final Assignment cancelled = new Assignment(1, task, DomainFixtures.user(2), AssignStatus.CANCELLED, LocalDateTime.now(), null, null);

        assertThatThrownBy(() -> cancelled.reject(LocalDateTime.now(), "x"))
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_ASSIGNMENT_STATE");
    }

    @Test
    void event_update_dates_requires_tasks_parameter_non_null() {
        final Event event = DomainFixtures.event(1, DomainFixtures.user(1));

        assertThatThrownBy(() -> event.updateDates(DomainFixtures.EVENT_RANGE_START, DomainFixtures.EVENT_RANGE_END, null))
                .hasFieldOrPropertyWithValue("errorCode", "EVENT_TASKS_REQUIRED");
    }
}
