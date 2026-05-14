package RUT.PlanningFlow.domain.service.matching.model;

import RUT.PlanningFlow.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchingResultTest {

    @Test
    void empty_factory_yields_zero_counts() {
        final MatchingResult empty = MatchingResult.empty();

        assertThat(empty.taskId()).isNull();
        assertThat(empty.requiredCount()).isZero();
        assertThat(empty.rankedCandidates()).isEmpty();
        assertThat(empty.rejectedCandidates()).isEmpty();
        assertThat(empty.shortageCount()).isZero();
        assertThat(empty.hasShortage()).isFalse();
        assertThat(empty.rankedCount()).isZero();
        assertThat(empty.rejectedCount()).isZero();
    }

    @Test
    void negative_required_count_rejected() {
        assertThatThrownBy(() -> new MatchingResult(1, -1, List.of(), List.of(), 0))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_REQUIRED_COUNT");
    }

    @Test
    void null_lists_normalized_to_empty() {
        final MatchingResult result = new MatchingResult(5, 2, null, null, 3);

        assertThat(result.rankedCandidates()).isEmpty();
        assertThat(result.rejectedCandidates()).isEmpty();
        assertThat(result.shortageCount()).isEqualTo(3);
        assertThat(result.hasShortage()).isTrue();
    }

    @Test
    void shortage_count_truncated_at_zero() {
        final MatchingResult result = new MatchingResult(1, 1, List.of(), List.of(), -5);

        assertThat(result.shortageCount()).isZero();
        assertThat(result.hasShortage()).isFalse();
    }
}
