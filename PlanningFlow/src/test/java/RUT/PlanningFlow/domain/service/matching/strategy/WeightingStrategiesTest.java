package RUT.PlanningFlow.domain.service.matching.strategy;

import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class WeightingStrategiesTest {

    @Test
    void standard_uses_balanced_weights_summing_to_one() {
        final WeightingStrategy s = WeightingStrategies.forMode(MatchingMode.STANDARD);

        assertThat(s).isInstanceOf(BalancedStrategy.class);
        assertThat(s.name()).isEqualTo("BALANCED");
        assertThat(s.getSkillWeight() + s.getGeoWeight() + s.getLoadWeight()).isCloseTo(1.0, within(1e-9));
        assertThat(s.getSkillWeight()).isEqualTo(0.40);
        assertThat(s.getGeoWeight()).isEqualTo(0.3);
        assertThat(s.getLoadWeight()).isEqualTo(0.3);
    }

    @Test
    void critical_uses_critical_strategy() {
        final WeightingStrategy s = WeightingStrategies.forMode(MatchingMode.CRITICAL);

        assertThat(s).isInstanceOf(CriticalStrategy.class);
        assertThat(s.name()).isEqualTo("CRITICAL");
        assertThat(s.getSkillWeight() + s.getGeoWeight() + s.getLoadWeight()).isCloseTo(1.0, within(1e-9));
        assertThat(s.getGeoWeight()).isEqualTo(0.6);
    }

    @Test
    void null_mode_rejected() {
        assertThatThrownBy(() -> WeightingStrategies.forMode(null))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "MATCHING_MODE_REQUIRED");
    }
}
