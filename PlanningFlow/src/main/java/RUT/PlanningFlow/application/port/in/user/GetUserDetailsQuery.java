package RUT.PlanningFlow.application.port.in.user;

import RUT.PlanningFlow.application.dto.user.UserResponseDto;

import java.util.Optional;

public interface GetUserDetailsQuery {
    Optional<UserResponseDto> execute(Integer userId);
}
