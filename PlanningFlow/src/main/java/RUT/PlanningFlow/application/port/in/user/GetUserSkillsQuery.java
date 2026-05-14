package RUT.PlanningFlow.application.port.in.user;

import RUT.PlanningFlow.application.dto.user.UserSkillResponseDto;

import java.util.List;

public interface GetUserSkillsQuery {
    List<UserSkillResponseDto> execute(Integer userId);
}
