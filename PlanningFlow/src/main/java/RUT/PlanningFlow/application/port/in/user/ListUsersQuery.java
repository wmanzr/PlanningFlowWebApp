package RUT.PlanningFlow.application.port.in.user;

import RUT.PlanningFlow.application.dto.user.UserResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.domain.enums.UserRoles;

public interface ListUsersQuery {
    PageResult<UserResponseDto> execute(
            Integer callerUserId,
            String username,
            UserRoles roleFilterOrNull,
            PageQuery pageQuery
    );
}
