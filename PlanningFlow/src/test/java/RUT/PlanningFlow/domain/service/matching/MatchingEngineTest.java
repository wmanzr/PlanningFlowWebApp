package RUT.PlanningFlow.domain.service.matching;

import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.service.matching.model.MatchingResult;
import RUT.PlanningFlow.domain.service.matching.model.RejectionReason;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchingEngineTest {

    private static final LocalDateTime T0 = DomainFixtures.EVENT_RANGE_START.plusHours(3);
    private static final LocalDateTime T1 = T0.plusHours(2);

    @Test
    void empty_candidates_yield_shortage_equal_required_count() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(100, event, creator, T0, T1);
        final MatchingContext context = DomainFixtures.matchingContext(
                T0.minusHours(1),
                DomainFixtures.defaultEventMode(),
                Map.of()
        );

        final MatchingEngine engine = new MatchingEngine();
        final MatchingResult result = engine.match(task, List.of(), 3, context);

        assertThat(result.rankedCandidates()).isEmpty();
        assertThat(result.shortageCount()).isEqualTo(3);
    }

    @Test
    void match_rejects_done_task() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.taskWithStatus(100, event, creator, TaskStatus.DONE, T0, T1);
        final MatchingContext context = DomainFixtures.matchingContext(
                LocalDateTime.now(),
                DomainFixtures.defaultEventMode(),
                Map.of()
        );

        final MatchingEngine engine = new MatchingEngine();

        assertThatThrownBy(() -> engine.match(task, List.of(DomainFixtures.user(2)), 1, context))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_TASK_CLOSED");
    }

    @Test
    void match_rejects_cancelled_task() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.taskWithStatus(100, event, creator, TaskStatus.CANCELLED, T0, T1);
        final MatchingContext context = DomainFixtures.matchingContext(
                LocalDateTime.now(),
                DomainFixtures.defaultEventMode(),
                Map.of()
        );

        final MatchingEngine engine = new MatchingEngine();

        assertThatThrownBy(() -> engine.match(task, List.of(DomainFixtures.user(2)), 1, context))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_TASK_CLOSED");
    }

    @Test
    void match_allows_assigned_task() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.taskWithStatus(100, event, creator, TaskStatus.ASSIGNED, T0, T1);
        final User candidate = DomainFixtures.user(10);
        final MatchingContext context = DomainFixtures.matchingContext(
                T0.minusHours(1),
                DomainFixtures.defaultEventMode(),
                Map.of(10, CandidateSnapshot.empty())
        );

        final MatchingEngine engine = new MatchingEngine();
        final MatchingResult result = engine.match(task, List.of(candidate), 1, context);

        assertThat(result.rankedCandidates()).hasSize(1);
    }

    @Test
    void phase_a_rejects_all_when_skills_missing() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(100, event, creator, T0, T1);
        task.addRequiredSkill(DomainFixtures.skill(50, "Must", "Medical"));
        final User candidate = DomainFixtures.user(10);
        final MatchingContext context = DomainFixtures.matchingContext(
                T0.minusHours(1),
                DomainFixtures.defaultEventMode(),
                Map.of(10, CandidateSnapshot.empty())
        );

        final MatchingEngine engine = new MatchingEngine();
        final MatchingResult result = engine.match(task, List.of(candidate), 1, context);

        assertThat(result.rankedCandidates()).isEmpty();
        assertThat(result.rejectedCandidates()).hasSize(1);
        assertThat(result.rejectedCandidates().getFirst().reason()).isEqualTo(RejectionReason.MISSING_REQUIRED_SKILLS);
        assertThat(result.shortageCount()).isEqualTo(1);
    }

    @Test
    void single_candidate_passes_phases_and_gets_ranked() {
        final User creator = DomainFixtures.user(1);
        final Event event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(100, event, creator, T0, T1);
        final User candidate = DomainFixtures.user(10);
        final MatchingContext context = DomainFixtures.matchingContext(
                T0.minusHours(1),
                DomainFixtures.defaultEventMode(),
                Map.of(10, CandidateSnapshot.empty())
        );

        final MatchingEngine engine = new MatchingEngine();
        final MatchingResult result = engine.match(task, List.of(candidate), 1, context);

        assertThat(result.rankedCandidates()).hasSize(1);
        assertThat(result.rankedCandidates().getFirst().rank()).isEqualTo(1);
        assertThat(result.shortageCount()).isZero();
    }
}
