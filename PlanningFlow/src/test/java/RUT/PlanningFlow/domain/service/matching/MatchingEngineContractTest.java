package RUT.PlanningFlow.domain.service.matching;

import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchingEngineContractTest {

    private static final LocalDateTime T0 = DomainFixtures.EVENT_RANGE_START.plusHours(3);
    private static final LocalDateTime T1 = T0.plusHours(2);

    @Test
    void match_requires_context_and_positive_count() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(1, event, creator, T0, T1);
        final MatchingEngine engine = new MatchingEngine();

        assertThatThrownBy(() -> engine.match(task, List.of(DomainFixtures.user(2)), 1, null))
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_CONTEXT_REQUIRED");

        assertThatThrownBy(() -> engine.match(task, List.of(), 0, DomainFixtures.matchingContext(
                LocalDateTime.now(),
                DomainFixtures.defaultEventMode(),
                Map.of()
        )))
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_REQUIRED_COUNT");
    }
}
