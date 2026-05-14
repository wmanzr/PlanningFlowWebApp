package RUT.PlanningFlow.application.port.out.repository;

import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.model.UserSkill;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findById(Integer id);
    List<User> findAll();
    PageResult<User> findUsers(PageQuery pageQuery);
    PageResult<User> findByUsernameContainingIgnoreCase(String searchTerm, PageQuery pageQuery);
    PageResult<User> findHavingRolesAndOptionalUsername(List<UserRoles> roles, String usernameTermOrNull, PageQuery pageQuery);

    PageResult<User> findUsersHavingRole(UserRoles role, PageQuery pageQuery);

    PageResult<User> findUsersHavingRoleAndUsernameContaining(UserRoles role, String term, PageQuery pageQuery);

    PageResult<User> findHavingDirectoryRoleAndFilterRole(
            List<UserRoles> directoryRoles,
            UserRoles filterRole,
            PageQuery pageQuery
    );

    PageResult<User> findHavingDirectoryRoleAndFilterRoleAndUsernameContaining(
            List<UserRoles> directoryRoles,
            UserRoles filterRole,
            String term,
            PageQuery pageQuery
    );
    List<User> findActiveParticipant();
    List<UserSkill> findSkillsForUser(Integer userId);

    long countAllUsers();

    Optional<Integer> create(User user);
    Optional<Integer> update(User user);
}