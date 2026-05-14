package RUT.PlanningFlow.domain.utils;

import RUT.PlanningFlow.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainAssertTest {

    @Test
    void not_null_throws_when_null() {
        assertThatThrownBy(() -> DomainAssert.notNull(null, "msg", "CODE_NULL"))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "CODE_NULL");
    }

    @Test
    void not_blank_throws_for_null_or_blank() {
        assertThatThrownBy(() -> DomainAssert.notBlank(null, "msg", "BLANK"))
                .hasFieldOrPropertyWithValue("errorCode", "BLANK");
        assertThatThrownBy(() -> DomainAssert.notBlank("  \t", "msg", "BLANK2"))
                .hasFieldOrPropertyWithValue("errorCode", "BLANK2");
    }

    @Test
    void is_true_throws_when_false() {
        assertThatThrownBy(() -> DomainAssert.isTrue(false, "msg", "NOT_TRUE"))
                .hasFieldOrPropertyWithValue("errorCode", "NOT_TRUE");
    }
}
