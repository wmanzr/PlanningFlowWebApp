package RUT.PlanningFlow.adapter.in.web.dto.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class UserSkillsUpdateRequest {

    @NotNull(message = "Список навыков обязателен")
    @Valid
    private List<UserSkillTierItem> skillTiers;

    public UserSkillsUpdateRequest() {
    }

    public List<UserSkillTierItem> getSkillTiers() {
        return skillTiers;
    }

    public void setSkillTiers(final List<UserSkillTierItem> skillTiers) {
        this.skillTiers = skillTiers;
    }
}
