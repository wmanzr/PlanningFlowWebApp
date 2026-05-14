package RUT.PlanningFlow.adapter.out.persistence.repository;

import RUT.PlanningFlow.adapter.out.persistence.entity.ResourceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ResourceRepository extends BaseRepository<ResourceEntity, Integer> {
    Page<ResourceEntity> findAllByOrderByNameAscIdAsc(Pageable pageable);
    Page<ResourceEntity> findByNameContainingIgnoreCaseOrderByNameAscIdAsc(String searchTerm, Pageable pageable);
}