package RUT.PlanningFlow.domain.service.matching;

import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.filter.CandidatePhaseAFilter;
import RUT.PlanningFlow.domain.service.matching.filter.CandidatePhaseBFilter;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.service.matching.model.MatchingResult;
import RUT.PlanningFlow.domain.service.matching.model.ScoreBreakdown;
import RUT.PlanningFlow.domain.service.matching.selection.FinalSelection;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingEngineOrchestrationMockTest {

    @Mock
    private CandidatePhaseAFilter phaseAFilter;

    @Mock
    private CandidatePhaseBFilter phaseBFilter;

    @Mock
    private FinalSelection finalSelection;

    @Test
    void uses_injected_collaborators_for_phases() {
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final LocalDateTime t0 = DomainFixtures.EVENT_RANGE_START.plusHours(3);
        final LocalDateTime t1 = t0.plusHours(2);
        final Task task = DomainFixtures.openTask(1, event, creator, t0, t1);
        final User candidate = DomainFixtures.user(10);
        final MatchingContext context = DomainFixtures.matchingContext(
                t0.minusHours(1),
                DomainFixtures.defaultEventMode(),
                Map.of()
        );

        when(phaseAFilter.rejectOrNull(any(), any(), any())).thenReturn(null);
        when(phaseBFilter.score(any(), any(), any())).thenReturn(new ScoreBreakdown(0.9d, 0.9d, 0.9d, 0.9d));
        when(finalSelection.select(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final MatchingEngine engine = new MatchingEngine(phaseAFilter, phaseBFilter, finalSelection);
        final MatchingResult result = engine.match(task, List.of(candidate), 1, context);

        assertThat(result.rankedCandidates()).hasSize(1);
        verify(phaseAFilter).rejectOrNull(any(), any(), any());
        verify(phaseBFilter).score(any(), any(), any());
        verify(finalSelection).select(any());
    }
}
