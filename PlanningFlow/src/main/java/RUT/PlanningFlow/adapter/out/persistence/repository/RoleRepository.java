package RUT.PlanningFlow.adapter.out.persistence.repository;

import RUT.PlanningFlow.adapter.out.persistence.entity.RoleEntity;
import RUT.PlanningFlow.domain.enums.UserRoles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface RoleRepository extends BaseRepository<RoleEntity, Integer> {
    Optional<RoleEntity> findByName(UserRoles name);
    Page<RoleEntity> findAllByOrderByNameAscIdAsc(Pageable pageable);
    Page<RoleEntity> findByNameContainingIgnoreCaseOrderByNameAscIdAsc(String searchTerm, Pageable pageable);
}