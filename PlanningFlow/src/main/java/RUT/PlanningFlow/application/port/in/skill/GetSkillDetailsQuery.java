package RUT.PlanningFlow.application.port.in.skill;

import RUT.PlanningFlow.application.dto.skill.SkillResponseDto;

import java.util.Optional;

public interface GetSkillDetailsQuery {
    Optional<SkillResponseDto> execute(Integer skillId);
}
