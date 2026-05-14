package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.enums.IncidentSeverity;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.model.Incident;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventTest {

    private static final LocalDateTime T0 = DomainFixtures.EVENT_RANGE_START.plusHours(1);
    private static final LocalDateTime T1 = T0.plusHours(2);

    @Nested
    @DisplayName("state transitions")
    class StateTransitions {

        @Test
        void start_planning_from_draft() {
            final User creator = DomainFixtures.user(1);
            final Event event = new Event(
                    1,
                    "Title",
                    null,
                    EventStatus.DRAFT,
                    DomainFixtures.EVENT_RANGE_START,
                    DomainFixtures.EVENT_RANGE_END,
                    null,
                    creator,
                    List.of(creator)
            );

            event.startPlanning();

            assertThat(event.getStatus()).isEqualTo(EventStatus.PLANNING);
        }

        @Test
        void activate_from_planning() {
            final User creator = DomainFixtures.user(1);
            final Event event = new Event(
                    1,
                    "Title",
                    null,
                    EventStatus.PLANNING,
                    DomainFixtures.EVENT_RANGE_START,
                    DomainFixtures.EVENT_RANGE_END,
                    null,
                    creator,
                    List.of()
            );

            event.activate();

            assertThat(event.getStatus()).isEqualTo(EventStatus.ACTIVE);
        }

        @Test
        void cancel_requires_reason() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);

            assertThatThrownBy(() -> event.cancel("  "))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "CANCEL_REASON_REQUIRED");
        }
    }

    @Nested
    @DisplayName("progress")
    class Progress {

        @Test
        void calculate_progress_empty_is_zero() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);

            assertThat(event.calculateProgress(null)).isZero();
            assertThat(event.calculateProgress(List.of())).isZero();
        }

        @Test
        void calculate_progress_half_done() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task done = DomainFixtures.taskWithStatus(1, event, creator, TaskStatus.DONE, T0, T1);
            final Task open = DomainFixtures.openTask(2, event, creator, T0, T1);

            assertThat(event.calculateProgress(List.of(done, open))).isEqualTo(50.0);
        }
    }

    @Nested
    @DisplayName("coordinators")
    class Coordinators {

        @Test
        void add_coordinator_deduplicates() {
            final User creator = DomainFixtures.user(1);
            final User coord = DomainFixtures.user(2);
            final Event event = DomainFixtures.event(1, creator);

            event.addCoordinator(coord);
            event.addCoordinator(coord);

            assertThat(event.getCoordinators()).hasSize(1);
        }

        @Test
        void effective_coordinator_falls_back_to_creator() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);

            assertThat(event.getEffectiveCoordinator()).isEqualTo(creator);
        }

        @Test
        void effective_coordinator_prefers_first_coordinator() {
            final User creator = DomainFixtures.user(1);
            final User coord = DomainFixtures.user(3);
            final Event event = DomainFixtures.event(1, creator);
            event.addCoordinator(coord);

            assertThat(event.getEffectiveCoordinator()).isEqualTo(coord);
        }

        @Test
        void clear_coordinators() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            event.addCoordinator(DomainFixtures.user(3));

            event.clearCoordinators();

            assertThat(event.getCoordinators()).isEmpty();
        }
    }

    @Nested
    @DisplayName("validation and completion")
    class ValidationAndCompletion {

        @Test
        void validate_task_attach_wrong_event_instance() {
            final User creator = DomainFixtures.user(1);
            final Event eventA = DomainFixtures.event(1, creator);
            final Event eventB = DomainFixtures.event(2, creator);
            final Task taskOnB = DomainFixtures.openTask(10, eventB, creator, T0, T1);

            assertThatThrownBy(() -> eventA.validateTaskAttach(taskOnB))
                    .hasFieldOrPropertyWithValue("errorCode", "TASK_WRONG_EVENT");
        }

        @Test
        void validate_task_attach_outside_schedule() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final Task task = DomainFixtures.openTask(10, event, creator, DomainFixtures.EVENT_RANGE_START.minusHours(1), T1);

            assertThatThrownBy(() -> event.validateTaskAttach(task))
                    .hasFieldOrPropertyWithValue("errorCode", "TASK_OUT_OF_EVENT_RANGE");
        }

        @Test
        void complete_when_only_finished_tasks() {
            final User creator = DomainFixtures.user(1);
            final Event event = new Event(
                    1,
                    "T",
                    null,
                    EventStatus.ACTIVE,
                    DomainFixtures.EVENT_RANGE_START,
                    DomainFixtures.EVENT_RANGE_END,
                    null,
                    creator,
                    List.of()
            );
            final Task done = DomainFixtures.taskWithStatus(1, event, creator, TaskStatus.DONE, T0, T1);

            event.complete(List.of(done));

            assertThat(event.getStatus()).isEqualTo(EventStatus.COMPLETED);
        }

        @Test
        void complete_rejects_when_tasks_assigned_or_in_progress() {
            final User creator = DomainFixtures.user(1);
            final Event event = new Event(
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
            final Task assigned = DomainFixtures.taskWithStatus(2, event, creator, TaskStatus.ASSIGNED, T0, T1);

            assertThatThrownBy(() -> event.complete(List.of(assigned)))
                    .hasFieldOrPropertyWithValue("errorCode", "INCOMPLETE_TASKS_FOR_EVENT_COMPLETION");
        }

        @Test
        void complete_rejects_when_open_tasks_remain() {
            final User creator = DomainFixtures.user(1);
            final Event event = new Event(
                    1,
                    "T",
                    null,
                    EventStatus.ACTIVE,
                    DomainFixtures.EVENT_RANGE_START,
                    DomainFixtures.EVENT_RANGE_END,
                    null,
                    creator,
                    List.of()
            );
            final Task open = DomainFixtures.openTask(2, event, creator, T0, T1);

            assertThatThrownBy(() -> event.complete(List.of(open)))
                    .hasFieldOrPropertyWithValue("errorCode", "INCOMPLETE_TASKS_FOR_EVENT_COMPLETION");
        }

        @Test
        void complete_when_done_and_cancelled_tasks() {
            final User creator = DomainFixtures.user(1);
            final Event event = new Event(
                    1,
                    "T",
                    null,
                    EventStatus.ACTIVE,
                    DomainFixtures.EVENT_RANGE_START,
                    DomainFixtures.EVENT_RANGE_END,
                    null,
                    creator,
                    List.of()
            );
            final Task done = DomainFixtures.taskWithStatus(1, event, creator, TaskStatus.DONE, T0, T1);
            final Task cancelled = DomainFixtures.taskWithStatus(2, event, creator, TaskStatus.CANCELLED, T0, T1);

            event.complete(List.of(done, cancelled));

            assertThat(event.getStatus()).isEqualTo(EventStatus.COMPLETED);
        }

        @Test
        void complete_resolves_open_incidents_automatically() {
            final User creator = DomainFixtures.user(1);
            final Event event = new Event(
                    1,
                    "T",
                    null,
                    EventStatus.ACTIVE,
                    DomainFixtures.EVENT_RANGE_START,
                    DomainFixtures.EVENT_RANGE_END,
                    null,
                    creator,
                    List.of()
            );
            final Task done = DomainFixtures.taskWithStatus(1, event, creator, TaskStatus.DONE, T0, T1);
            final Incident incident = new Incident(
                    10,
                    event,
                    null,
                    null,
                    creator,
                    "Дым на площадке",
                    IncidentSeverity.MEDIUM,
                    IncidentStatus.OPEN,
                    T0,
                    null,
                    null
            );

            event.complete(List.of(done), List.of(incident));

            assertThat(event.getStatus()).isEqualTo(EventStatus.COMPLETED);
            assertThat(incident.getStatus()).isEqualTo(IncidentStatus.RESOLVED);
            assertThat(incident.getResolutionNotes()).contains("автоматически");
        }

        @Test
        void cancel_completed_event_rejected() {
            final User creator = DomainFixtures.user(1);
            final Event event = new Event(
                    1,
                    "T",
                    null,
                    EventStatus.COMPLETED,
                    DomainFixtures.EVENT_RANGE_START,
                    DomainFixtures.EVENT_RANGE_END,
                    null,
                    creator,
                    List.of()
            );

            assertThatThrownBy(() -> event.cancel("reason"))
                    .hasFieldOrPropertyWithValue("errorCode", "INVALID_EVENT_STATE");
        }

        @Test
        void update_info_changes_title() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);

            event.updateInfo("NewTitle", "Desc");

            assertThat(event.getTitle()).isEqualTo("NewTitle");
            assertThat(event.getDescription()).isEqualTo("Desc");
        }

        @Test
        void update_location_and_clear() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);

            event.updateLocation(DomainFixtures.moscowCenter());
            assertThat(event.getLocation()).isNotNull();

            event.clearLocation();
            assertThat(event.getLocation()).isNull();
        }

        @Test
        void reschedule_updates_schedule_when_tasks_fit() {
            final User creator = DomainFixtures.user(1);
            final Event event = DomainFixtures.event(1, creator);
            final LocalDateTime ns = DomainFixtures.EVENT_RANGE_START.plusHours(1);
            final LocalDateTime ne = DomainFixtures.EVENT_RANGE_END.minusHours(1);
            final Task task = DomainFixtures.openTask(5, event, creator, ns.plusHours(1), ns.plusHours(3));

            event.reschedule(ns, ne, List.of(task));

            assertThat(event.getStartDate()).isEqualTo(ns);
            assertThat(event.getEndDate()).isEqualTo(ne);
        }
    }
}
