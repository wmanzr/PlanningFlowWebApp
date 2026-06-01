package RUT.PlanningFlow.application.service.user;

import RUT.PlanningFlow.application.dto.user.UserSkillResponseDto;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.UserSkill;
import org.springframework.stereotype.Component;

@Component
public final class UserSkillResponseDtoMapper {

    public UserSkillResponseDto toResponse(final UserSkill us) {
        if (us == null) {
            return null;
        }
        final Skill skill = us.getSkill();
        return new UserSkillResponseDto(
                us.getId(),
                skill == null ? null : skill.getId(),
                skill == null ? null : skill.getName(),
                us.getTier(),
                us.getVerifiedAt()
        );
    }
}
