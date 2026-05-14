package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkillTest {

    @Test
    void rename_normalizes_and_updates_name() {
        final Skill skill = new Skill(1, " CPR ", "Medical");

        skill.rename("  Advanced CPR  ");

        assertThat(skill.getName()).isEqualTo("Advanced CPR");
    }

    @Test
    void blank_category_rejected_on_construct() {
        assertThatThrownBy(() -> new Skill(1, "Name", " "))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "SKILL_CATEGORY_REQUIRED");
    }

    @Test
    void assert_catalog_name_not_taken_throws_when_true() {
        assertThatThrownBy(() -> Skill.assertCatalogNameNotTaken(true))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "SKILL_NAME_ALREADY_EXISTS");
    }

    @Test
    void change_category_updates_value() {
        final Skill skill = new Skill(2, "Name", "OldCat");

        skill.changeCategory(" NewCat ");

        assertThat(skill.getCategory()).isEqualTo("NewCat");
    }

    @Test
    void equals_and_hash_code_use_id() {
        final Skill a = new Skill(4, "A", "C1");
        final Skill b = new Skill(4, "B", "C2");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(new Skill(5, "A", "C1"));
    }
}
