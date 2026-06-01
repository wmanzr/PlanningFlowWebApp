package RUT.PlanningFlow.application.service.user;

import RUT.PlanningFlow.application.dto.user.UserResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.config.cache.ApplicationRedisCacheConfiguration;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class ListUsersDirectoryCacheFacade {

    private static final List<UserRoles> DIRECTORY_ROLES = List.of(
            UserRoles.ORGANIZER,
            UserRoles.COORDINATOR,
            UserRoles.PARTICIPANT
    );

    private final UserRepositoryPort userRepository;
    private final UserResponseDtoMapper userResponseDtoMapper;

    public ListUsersDirectoryCacheFacade(
            final UserRepositoryPort userRepository,
            final UserResponseDtoMapper userResponseDtoMapper
    ) {
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userResponseDtoMapper, "Маппер ответа по пользователю обязателен", "USER_RESPONSE_DTO_MAPPER_REQUIRED");
        this.userRepository = userRepository;
        this.userResponseDtoMapper = userResponseDtoMapper;
    }

    @Cacheable(
            cacheNames = ApplicationRedisCacheConfiguration.CACHE_USERS_DIRECTORY,
            key = "#callerUserId + '|' + #callerIsAdmin + '|' + (#username ?: '') + '|' + (#roleFilterOrNull?.name() ?: '') + '|' + #pageQuery.page + '|' + #pageQuery.size"
    )
    public PageResult<UserResponseDto> loadPage(
            final Integer callerUserId,
            final boolean callerIsAdmin,
            final String username,
            final UserRoles roleFilterOrNull,
            final PageQuery pageQuery
    ) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");

        final String term = username == null || username.isBlank() ? null : username.trim();

        final PageResult<User> page = callerIsAdmin
                ? resolvePageForAdmin(term, roleFilterOrNull, pageQuery)
                : resolvePageForPlanner(term, roleFilterOrNull, pageQuery);

        final List<UserResponseDto> items = new ArrayList<>(page.items().size());
        for (final User u : page.items()) {
            items.add(userResponseDtoMapper.toResponse(u));
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