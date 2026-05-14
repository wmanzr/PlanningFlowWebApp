package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.SkillTier;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserSkillTest {

    @Test
    void update_tier_skips_when_same_value() {
        final User user = DomainFixtures.user(1);
        final Skill skill = DomainFixtures.skill(5, "RLS", "Safety");
        final LocalDateTime verified = LocalDateTime.of(2026, 5, 1, 12, 0);
        final UserSkill userSkill = new UserSkill(10, user, skill, SkillTier.NOVICE, verified);

        userSkill.updateTier(SkillTier.NOVICE);

        assertThat(userSkill.getTier()).isEqualTo(SkillTier.NOVICE);
        assertThat(userSkill.getVerifiedAt()).isEqualTo(verified);
    }

    @Test
    void update_tier_changes_level_and_verified_at() {
        final User user = DomainFixtures.user(1);
        final Skill skill = DomainFixtures.skill(5, "RLS", "Safety");
        final UserSkill userSkill = new UserSkill(10, user, skill, SkillTier.NOVICE, LocalDateTime.of(2026, 5, 1, 12, 0));

        userSkill.updateTier(SkillTier.EXPERT);

        assertThat(userSkill.getTier()).isEqualTo(SkillTier.EXPERT);
        assertThat(userSkill.getVerifiedAt()).isNotNull();
    }

    @Test
    void constructor_requires_user_skill_and_tier() {
        final User user = DomainFixtures.user(1);
        final Skill skill = DomainFixtures.skill(1, "X", "Y");

        assertThatThrownBy(() -> new UserSkill(1, null, skill, SkillTier.EXPERT, null))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "USER_REQUIRED");

        assertThatThrownBy(() -> new UserSkill(1, user, null, SkillTier.EXPERT, null))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "SKILL_REQUIRED");

        assertThatThrownBy(() -> new UserSkill(1, user, skill, null, null))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "SKILL_TIER_REQUIRED");
    }

    @Test
    void update_tier_requires_non_null() {
        final UserSkill userSkill = new UserSkill(
                1,
                DomainFixtures.user(1),
                DomainFixtures.skill(1, "X", "Y"),
                SkillTier.NOVICE,
                null
        );

        assertThatThrownBy(() -> userSkill.updateTier(null))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "SKILL_TIER_REQUIRED");
    }

    @Test
    void equals_and_hash_code_use_id() {
        final UserSkill a = new UserSkill(7, DomainFixtures.user(1), DomainFixtures.skill(1, "X", "Y"), SkillTier.EXPERT, null);
        final UserSkill b = new UserSkill(7, DomainFixtures.user(2), DomainFixtures.skill(2, "Z", "W"), SkillTier.NOVICE, null);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(new UserSkill(8, DomainFixtures.user(1), DomainFixtures.skill(1, "X", "Y"), SkillTier.EXPERT, null));
    }
}
