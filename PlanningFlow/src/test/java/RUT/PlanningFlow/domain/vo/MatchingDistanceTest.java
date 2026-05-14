package RUT.PlanningFlow.domain.vo;

import RUT.PlanningFlow.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchingDistanceTest {

    @Test
    void non_positive_radius_rejected() {
        assertThatThrownBy(() -> new MatchingDistance(0.0))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_MATCHING_DISTANCE_RADIUS");
    }
}
