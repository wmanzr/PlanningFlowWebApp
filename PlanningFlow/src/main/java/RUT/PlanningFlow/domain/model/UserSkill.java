package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.SkillTier;
import RUT.PlanningFlow.domain.utils.DomainAssert;

import java.time.LocalDateTime;

public class UserSkill {
    private final Integer id;
    private final User user;
    private final Skill skill;
    private SkillTier tier;
    private LocalDateTime verifiedAt;

    public UserSkill(
            final Integer id,
            final User user,
            final Skill skill,
            final SkillTier tier,
            final LocalDateTime verifiedAt
    ) {
        this.id = id;
        DomainAssert.notNull(user, "Пользователь навыка обязателен", "USER_REQUIRED");
        DomainAssert.notNull(skill, "Навык обязателен", "SKILL_REQUIRED");
        this.user = user;
        this.skill = skill;
        DomainAssert.notNull(tier, "Уровень владения навыком обязателен", "SKILL_TIER_REQUIRED");
        this.tier = tier;
        this.verifiedAt = verifiedAt;
    }

    public void updateTier(final SkillTier newTier) {
        DomainAssert.notNull(newTier, "Уровень владения навыком обязателен", "SKILL_TIER_REQUIRED");
        if (this.tier == newTier) {
            return;
        }
        this.tier = newTier;
        this.verifiedAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Skill getSkill() {
        return skill;
    }

    public SkillTier getTier() {
        return tier;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UserSkill that = (UserSkill) o;
        return id != null && that.id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
