package RUT.PlanningFlow.application.service.skill;

import RUT.PlanningFlow.application.dto.skill.SkillResponseDto;
import RUT.PlanningFlow.domain.model.Skill;
import org.springframework.stereotype.Component;

@Component
public final class SkillResponseDtoMapper {

    public SkillResponseDto toResponse(final Skill skill) {
        if (skill == null) {
            return null;
        }
        return new SkillResponseDto(skill.getId(), skill.getName(), skill.getCategory());
    }
}
