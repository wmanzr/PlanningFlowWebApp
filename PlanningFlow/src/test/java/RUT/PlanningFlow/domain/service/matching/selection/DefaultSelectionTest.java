package RUT.PlanningFlow.domain.service.matching.selection;

import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.RankedCandidate;
import RUT.PlanningFlow.domain.service.matching.model.ScoreBreakdown;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultSelectionTest {

    private final DefaultSelection selection = new DefaultSelection();

    @Test
    void orders_by_total_then_skill_then_geo_then_ids() {
        final User lowId = DomainFixtures.user(1);
        final User highId = DomainFixtures.user(2);
        final ScoreBreakdown sameTop = new ScoreBreakdown(0.9, 0.5, 0.4, 0.3);
        final ScoreBreakdown sameAll = new ScoreBreakdown(0.8, 0.8, 0.8, 0.8);

        final List<RankedCandidate> input = List.of(
                new RankedCandidate(highId, sameAll, 0),
                new RankedCandidate(lowId, sameAll, 0),
                new RankedCandidate(lowId, new ScoreBreakdown(0.95, 0.6, 0.5, 0.2), 0),
                new RankedCandidate(highId, sameTop, 0)
        );

        final List<RankedCandidate> ordered = selection.select(input);

        assertThat(ordered.get(0).score().totalScore()).isEqualTo(0.95);
        assertThat(ordered.get(1).score().totalScore()).isEqualTo(0.9);
        assertThat(ordered.get(2).candidate().getId()).isEqualTo(1);
        assertThat(ordered.get(3).candidate().getId()).isEqualTo(2);
    }

    @Test
    void null_input_rejected() {
        assertThatThrownBy(() -> selection.select(null))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "FINAL_SELECTION_CANDIDATES_REQUIRED");
    }
}
