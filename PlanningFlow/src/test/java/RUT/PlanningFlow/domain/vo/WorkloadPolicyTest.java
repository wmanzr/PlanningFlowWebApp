package RUT.PlanningFlow.domain.vo;

import RUT.PlanningFlow.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkloadPolicyTest {

    @Test
    void negative_max_daily_load_rejected() {
        assertThatThrownBy(() -> new WorkloadPolicy(Duration.ofHours(-1), Duration.ZERO))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_MAX_DAILY_LOAD");
    }

    @Test
    void negative_min_technical_gap_rejected() {
        assertThatThrownBy(() -> new WorkloadPolicy(Duration.ofHours(2), Duration.ofMinutes(-1)))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_MIN_TECHNICAL_GAP");
    }

    @Test
    void defaults_are_non_negative_durations() {
        final WorkloadPolicy policy = WorkloadPolicy.defaults();

        assertThat(policy.maxDailyLoad().isNegative()).isFalse();
        assertThat(policy.minTechnicalGap().isNegative()).isFalse();
    }
}
