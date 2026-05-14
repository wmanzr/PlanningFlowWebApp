package RUT.PlanningFlow.domain.service.matching.model;

import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RankedCandidateAndRejectedCandidateTest {

    @Test
    void ranked_candidate_requires_candidate_and_score() {
        final User user = DomainFixtures.user(1);
        final ScoreBreakdown score = new ScoreBreakdown(1.0, 1.0, 1.0, 1.0);

        assertThatThrownBy(() -> new RankedCandidate(null, score, 1))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_CANDIDATE_REQUIRED");

        assertThatThrownBy(() -> new RankedCandidate(user, null, 1))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_SCORE_REQUIRED");
    }

    @Test
    void rejected_candidate_requires_fields() {
        final User user = DomainFixtures.user(1);

        assertThatThrownBy(() -> new RejectedCandidate(null, RejectionReason.TIME_CONFLICT, "x"))
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_CANDIDATE_REQUIRED");

        assertThatThrownBy(() -> new RejectedCandidate(user, null, "x"))
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_REJECTION_REASON_REQUIRED");
    }
}
