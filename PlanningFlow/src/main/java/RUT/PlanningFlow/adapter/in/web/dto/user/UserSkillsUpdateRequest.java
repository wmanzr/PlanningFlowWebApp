package RUT.PlanningFlow.adapter.in.web.dto.user;

import RUT.PlanningFlow.domain.enums.SkillTier;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public Map<Integer, SkillTier> toTierMap() {
        if (skillTiers == null || skillTiers.isEmpty()) {
            return Map.of();
        }
        final Map<Integer, SkillTier> map = new LinkedHashMap<>();
        for (final UserSkillTierItem item : skillTiers) {
            map.put(item.getSkillId(), item.getTier());
        }
        return map;
    }
}