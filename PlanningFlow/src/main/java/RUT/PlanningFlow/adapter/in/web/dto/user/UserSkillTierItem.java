package RUT.PlanningFlow.adapter.in.web.dto.user;

import RUT.PlanningFlow.domain.enums.SkillTier;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class UserSkillTierItem {

    @NotNull(message = "ID навыка обязателен")
    @Positive(message = "ID навыка должен быть положительным")
    private Integer skillId;

    @NotNull(message = "Уровень владения обязателен")
    private SkillTier tier;

    public UserSkillTierItem() {
    }

    public Integer getSkillId() {
        return skillId;
    }

    public void setSkillId(final Integer skillId) {
        this.skillId = skillId;
    }

    public SkillTier getTier() {
        return tier;
    }

    public void setTier(final SkillTier tier) {
        this.tier = tier;
    }
}