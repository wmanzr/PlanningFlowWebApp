package RUT.PlanningFlow.domain.vo;

import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventModeTest {

    @Test
    void null_matching_mode_rejected() {
        assertThatThrownBy(() -> new EventMode(null, MatchingDistance.BUILDING_SCALE, WorkloadPolicy.defaults()))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_MODE_REQUIRED");
    }
}
