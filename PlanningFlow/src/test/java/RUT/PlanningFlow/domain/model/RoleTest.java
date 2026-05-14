package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleTest {

    @Test
    void constructor_requires_role_name() {
        assertThatThrownBy(() -> new Role(1, null))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "ROLE_NAME_REQUIRED");
    }

    @Test
    void getters_and_value_identity() {
        final Role role = new Role(3, UserRoles.ADMIN);

        assertThat(role.getId()).isEqualTo(3);
        assertThat(role.getName()).isEqualTo(UserRoles.ADMIN);
    }

    @Test
    void equals_and_hash_code_by_id() {
        final Role a = new Role(9, UserRoles.PARTICIPANT);
        final Role b = new Role(9, UserRoles.COORDINATOR);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
