package RUT.PlanningFlow.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    @Test
    void exposes_message_and_code() {
        final DomainException ex = new DomainException("hello", "E1");

        assertThat(ex.getMessage()).isEqualTo("hello");
        assertThat(ex.getErrorCode()).isEqualTo("E1");
    }
}
