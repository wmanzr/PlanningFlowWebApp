package RUT.PlanningFlow.application.service.user;

import RUT.PlanningFlow.application.dto.user.UserResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.user.ListUsersQuery;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListUsersService implements ListUsersQuery {

    private static final List<UserRoles> DIRECTORY_ROLES = List.of(
            UserRoles.ORGANIZER,
            UserRoles.COORDINATOR,
            UserRoles.PARTICIPANT
    );

    private final UserRepositoryPort userRepository;

    public ListUsersService(final UserRepositoryPort userRepository) {
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        this.userRepository = userRepository;
    }

    @Override
    public PageResult<UserResponseDto> execute(
            final Integer callerUserId,
            final String username,
            final UserRoles roleFilterOrNull,
            final PageQuery pageQuery
    ) {
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");

        final User caller = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!PlanningAccessPolicy.hasRole(caller, UserRoles.ADMIN)
                && !PlanningAccessPolicy.hasRole(caller, UserRoles.ORGANIZER)
                && !PlanningAccessPolicy.hasRole(caller, UserRoles.COORDINATOR)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final String term = username == null || username.isBlank() ? null : username.trim();

        final PageResult<User> page;
        if (PlanningAccessPolicy.hasRole(caller, UserRoles.ADMIN)) {
            page = resolvePageForAdmin(term, roleFilterOrNull, pageQuery);
        } else {
            page = resolvePageForPlanner(term, roleFilterOrNull, pageQuery);
        }

        final List<UserResponseDto> items = new ArrayList<>(page.items().size());
        for (final User u : page.items()) {
            items.add(UserResponseDto.from(u));
        }
        return new PageResult<>(items, page.totalElements(), page.totalPages());
    }

    private PageResult<User> resolvePageForAdmin(
            final String usernameTermOrNull,
            final UserRoles roleFilterOrNull,
            final PageQuery pageQuery
    ) {
        if (roleFilterOrNull == null) {
            return usernameTermOrNull == null
                    ? userRepository.findUsers(pageQuery)
                    : userRepository.findByUsernameContainingIgnoreCase(usernameTermOrNull, pageQuery);
        }
        return usernameTermOrNull == null
                ? userRepository.findUsersHavingRole(roleFilterOrNull, pageQuery)
                : userRepository.findUsersHavingRoleAndUsernameContaining(
                        roleFilterOrNull,
                        usernameTermOrNull,
                        pageQuery
                );
    }

    private PageResult<User> resolvePageForPlanner(
            final String usernameTermOrNull,
            final UserRoles roleFilterOrNull,
            final PageQuery pageQuery
    ) {
        if (roleFilterOrNull == null) {
            return userRepository.findHavingRolesAndOptionalUsername(DIRECTORY_ROLES, usernameTermOrNull, pageQuery);
        }
        return usernameTermOrNull == null
                ? userRepository.findHavingDirectoryRoleAndFilterRole(DIRECTORY_ROLES, roleFilterOrNull, pageQuery)
                : userRepository.findHavingDirectoryRoleAndFilterRoleAndUsernameContaining(
                        DIRECTORY_ROLES,
                        roleFilterOrNull,
                        usernameTermOrNull,
                        pageQuery
                );
    }
}
