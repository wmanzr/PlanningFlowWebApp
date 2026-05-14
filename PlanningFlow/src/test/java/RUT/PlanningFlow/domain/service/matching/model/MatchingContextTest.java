package RUT.PlanningFlow.domain.service.matching.model;

import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import RUT.PlanningFlow.domain.vo.EventMode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchingContextTest {

    @Test
    void snapshot_for_user_without_id_returns_empty_snapshot() {
        final User creator = DomainFixtures.user(1);
        final EventMode mode = DomainFixtures.defaultEventMode();
        final MatchingContext context = new MatchingContext(LocalDateTime.now(), mode, Map.of(), Set.of());

        final User noId = new User(null, "x", "p", "e@e.com", "N", java.time.LocalDate.now().minusYears(20), java.util.List.of());

        assertThat(context.snapshotFor(noId)).isEqualTo(CandidateSnapshot.empty());
    }

    @Test
    void snapshot_for_returns_registered_or_empty() {
        final User u10 = DomainFixtures.user(10);
        final CandidateSnapshot snap = CandidateSnapshot.empty();
        final MatchingContext context = DomainFixtures.matchingContext(
                LocalDateTime.now(),
                DomainFixtures.defaultEventMode(),
                Map.of(10, snap)
        );

        assertThat(context.snapshotFor(u10)).isSameAs(snap);
        assertThat(context.snapshotFor(DomainFixtures.user(11))).isEqualTo(CandidateSnapshot.empty());
    }

    @Test
    void constructor_requires_now_and_event_mode() {
        assertThatThrownBy(() -> new MatchingContext(null, DomainFixtures.defaultEventMode(), Map.of(), Set.of()))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_NOW_REQUIRED");

        assertThatThrownBy(() -> new MatchingContext(LocalDateTime.now(), null, Map.of(), Set.of()))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "EVENT_MODE_REQUIRED");
    }

    @Test
    void null_snapshots_map_normalized_to_empty() {
        final MatchingContext context = new MatchingContext(
                LocalDateTime.now(),
                DomainFixtures.defaultEventMode(),
                null,
                Set.of()
        );

        assertThat(context.snapshotFor(DomainFixtures.user(10))).isEqualTo(CandidateSnapshot.empty());
    }

    @Test
    void snapshot_for_requires_candidate() {
        final MatchingContext context = DomainFixtures.matchingContext(
                LocalDateTime.now(),
                DomainFixtures.defaultEventMode(),
                Map.of()
        );

        assertThatThrownBy(() -> context.snapshotFor(null))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_CANDIDATE_REQUIRED");
    }
}
