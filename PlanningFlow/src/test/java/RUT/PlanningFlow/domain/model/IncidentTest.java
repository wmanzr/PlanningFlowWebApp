package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.IncidentSeverity;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncidentTest {

    private static final LocalDateTime T0 = DomainFixtures.EVENT_RANGE_START.plusHours(2);
    private static final LocalDateTime T1 = T0.plusHours(2);

    @Test
    void mark_in_progress_from_open() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final InternalResource resource = new InternalResource(1, "Tool", ResourceType.EQUIPMENT, "INV-1");
        final Incident incident = new Incident(
                1,
                event,
                task,
                resource,
                creator,
                "Something broke",
                IncidentSeverity.HIGH,
                IncidentStatus.OPEN,
                LocalDateTime.now(),
                null,
                null
        );

        incident.markAsInProgress();

        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.IN_PROGRESS);
    }

    @Test
    void resolve_sets_resolved_status() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Incident incident = new Incident(
                1,
                event,
                null,
                null,
                creator,
                "Leak",
                IncidentSeverity.MEDIUM,
                IncidentStatus.IN_PROGRESS,
                LocalDateTime.now(),
                null,
                null
        );

        incident.resolve("Fixed valve");

        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.RESOLVED);
        assertThat(incident.getResolutionNotes()).isEqualTo("Fixed valve");
    }

    @Test
    void resolve_requires_notes() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Incident incident = new Incident(
                1,
                event,
                null,
                null,
                creator,
                "Leak",
                IncidentSeverity.MEDIUM,
                IncidentStatus.IN_PROGRESS,
                LocalDateTime.now(),
                null,
                null
        );

        assertThatThrownBy(() -> incident.resolve(" "))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INCIDENT_RESOLUTION_NOTES_REQUIRED");
    }

    @Test
    void getters_surface_linked_entities_and_notes() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final InternalResource resource = new InternalResource(1, "Tool", ResourceType.EQUIPMENT, "INV-1");
        final LocalDateTime created = LocalDateTime.of(2026, 4, 1, 10, 0);
        final LocalDateTime resolvedAt = LocalDateTime.of(2026, 4, 2, 15, 30);

        final Incident incident = new Incident(
                42,
                event,
                task,
                resource,
                creator,
                "Smoke detected",
                IncidentSeverity.HIGH,
                IncidentStatus.RESOLVED,
                created,
                resolvedAt,
                "Inspected wiring"
        );

        assertThat(incident.getId()).isEqualTo(42);
        assertThat(incident.getEvent()).isSameAs(event);
        assertThat(incident.getTask()).isSameAs(task);
        assertThat(incident.getResource()).isSameAs(resource);
        assertThat(incident.getReporter()).isSameAs(creator);
        assertThat(incident.getDescription()).isEqualTo("Smoke detected");
        assertThat(incident.getSeverity()).isEqualTo(IncidentSeverity.HIGH);
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.RESOLVED);
        assertThat(incident.getCreatedAt()).isEqualTo(created);
        assertThat(incident.getResolvedAt()).isEqualTo(resolvedAt);
        assertThat(incident.getResolutionNotes()).isEqualTo("Inspected wiring");
    }
}
