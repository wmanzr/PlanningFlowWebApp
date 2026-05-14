package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void rename_updates_full_name() {
        final User user = DomainFixtures.user(1);

        user.rename("Новое имя");

        assertThat(user.getFullName()).isEqualTo("Новое имя");
    }

    @Test
    void birth_date_below_minimum_age_rejected() {
        final LocalDate tooYoung = LocalDate.now().minusYears(13);
        assertThatThrownBy(() -> new User(1, "u", "p", "e@e.com", "Name", tooYoung, List.of()))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_USER_AGE");
    }

    @Test
    void change_email_and_password() {
        final User user = DomainFixtures.user(1);

        user.changeEmail("new@example.com");
        user.changePassword("new-secret");

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getPassword()).isEqualTo("new-secret");
    }

    @Test
    void remove_role_removes_matching_enum() {
        final User user = DomainFixtures.user(1);
        user.addRole(new Role(1, UserRoles.ADMIN));

        user.removeRole(new Role(9, UserRoles.ADMIN));

        assertThat(user.getRoles()).isEmpty();
    }

    @Test
    void add_role_deduplicates_by_enum() {
        final User user = DomainFixtures.user(1);
        final List<Role> roles = new ArrayList<>();
        roles.add(new Role(1, UserRoles.COORDINATOR));
        final User withRoles = new User(
                2,
                "coord",
                "p",
                "c@e.com",
                "C",
                LocalDate.now().minusYears(25),
                roles
        );

        withRoles.addRole(new Role(2, UserRoles.COORDINATOR));

        assertThat(withRoles.getRoles()).hasSize(1);
    }
}
