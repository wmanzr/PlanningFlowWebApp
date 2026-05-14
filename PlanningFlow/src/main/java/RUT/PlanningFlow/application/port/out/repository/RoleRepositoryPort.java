package RUT.PlanningFlow.application.port.out.repository;

import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepositoryPort {
    Optional<Role> findByName(UserRoles name);
    Optional<Role> findById(Integer id);
    List<Role> findAll();
    PageResult<Role> findRoles(PageQuery pageQuery);
    PageResult<Role> findByNameContainingIgnoreCase(String searchTerm, PageQuery pageQuery);
    Optional<Integer> create(Role role);
    Optional<Integer> update(Role role);
}