package RUT.PlanningFlow.application.port.in.user;

import RUT.PlanningFlow.domain.enums.SkillTier;

import java.util.List;
import java.util.Map;

public interface ManageUserSkillsUseCase {
    List<Integer> execute(Integer userId, Map<Integer, SkillTier> skillTiers);
}