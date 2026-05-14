package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssignmentTest {

    private static final LocalDateTime T0 = DomainFixtures.EVENT_RANGE_START.plusHours(2);
    private static final LocalDateTime T1 = T0.plusHours(2);

    @Test
    void accept_from_pending() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final User volunteer = DomainFixtures.user(2);
        final LocalDateTime assignedAt = LocalDateTime.of(2026, 6, 1, 10, 0);
        final Assignment assignment = DomainFixtures.pendingAssignment(1, task, volunteer, assignedAt);
        final LocalDateTime response = assignedAt.plusMinutes(5);

        assignment.accept(response);

        assertThat(assignment.getStatus()).isEqualTo(AssignStatus.ACCEPTED);
        assertThat(assignment.getRespondedAt()).isEqualTo(response);
    }

    @Test
    void reject_requires_reason() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final Assignment assignment = DomainFixtures.pendingAssignment(
                1,
                task,
                DomainFixtures.user(2),
                LocalDateTime.now()
        );

        assertThatThrownBy(() -> assignment.reject(LocalDateTime.now(), " "))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "REJECTION_REASON_REQUIRED");
    }

    @Test
    void reject_from_pending_with_reason() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final Assignment assignment = DomainFixtures.pendingAssignment(1, task, DomainFixtures.user(2), LocalDateTime.now());
        final LocalDateTime response = LocalDateTime.now();

        assignment.reject(response, "busy");

        assertThat(assignment.getStatus()).isEqualTo(AssignStatus.REJECTED);
        assertThat(assignment.getRejectionReason()).isEqualTo("busy");
        assertThat(assignment.getRespondedAt()).isEqualTo(response);
    }

    @Test
    void accept_is_idempotent_when_already_accepted() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final Assignment assignment = DomainFixtures.pendingAssignment(1, task, DomainFixtures.user(2), LocalDateTime.now());
        final LocalDateTime response = LocalDateTime.now();
        assignment.accept(response);

        assignment.accept(response.plusMinutes(1));

        assertThat(assignment.getStatus()).isEqualTo(AssignStatus.ACCEPTED);
    }

    @Test
    void cancel_by_coordinator_from_pending() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final Assignment assignment = DomainFixtures.pendingAssignment(1, task, DomainFixtures.user(2), LocalDateTime.now());

        assignment.cancelByCoordinator(LocalDateTime.now());

        assertThat(assignment.getStatus()).isEqualTo(AssignStatus.CANCELLED);
    }
}
