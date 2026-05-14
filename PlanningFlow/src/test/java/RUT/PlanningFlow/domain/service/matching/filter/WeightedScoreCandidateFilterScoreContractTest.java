package RUT.PlanningFlow.domain.service.matching.filter;

import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeightedScoreCandidateFilterScoreContractTest {

    private final WeightedScoreCandidateFilter filter = WeightedScoreCandidateFilter.balancedDefaults();

    @Test
    void score_requires_task_candidate_and_context() {
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final LocalDateTime t0 = DomainFixtures.EVENT_RANGE_START.plusHours(2);
        final Task task = DomainFixtures.openTask(1, event, creator, t0, t0.plusHours(2));
        final User candidate = DomainFixtures.user(10);
        final MatchingContext ctx = DomainFixtures.matchingContext(t0.minusHours(1), DomainFixtures.defaultEventMode(), Map.of());

        assertThatThrownBy(() -> filter.score(null, candidate, ctx))
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_TASK_REQUIRED");

        assertThatThrownBy(() -> filter.score(task, null, ctx))
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_CANDIDATE_REQUIRED");

        assertThatThrownBy(() -> filter.score(task, candidate, null))
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_CONTEXT_REQUIRED");
    }
}
