package RUT.PlanningFlow.application.service.user;

import RUT.PlanningFlow.application.dto.user.UserResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.user.ListUsersQuery;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListUsersService implements ListUsersQuery {

    private final UserRepositoryPort userRepository;
    private final ListUsersDirectoryCacheFacade directoryCache;

    public ListUsersService(
            final UserRepositoryPort userRepository,
            final ListUsersDirectoryCacheFacade directoryCache
    ) {
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(directoryCache, "Кэш каталога участников обязателен", "USER_DIRECTORY_CACHE_REQUIRED");
        this.userRepository = userRepository;
        this.directoryCache = directoryCache;
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
                .orElseThrow(() -> new DomainException("Пользователь не найден", "USER_NOT_FOUND"));

        if (!PlanningAccessPolicy.hasRole(caller, UserRoles.ADMIN)
                && !PlanningAccessPolicy.hasRole(caller, UserRoles.ORGANIZER)
                && !PlanningAccessPolicy.hasRole(caller, UserRoles.COORDINATOR)) {
            throw new DomainException("Доступ запрещён", "ACCESS_DENIED");
        }

        final boolean callerIsAdmin = PlanningAccessPolicy.hasRole(caller, UserRoles.ADMIN);
        return directoryCache.loadPage(callerUserId, callerIsAdmin, username, roleFilterOrNull, pageQuery);
    }
}