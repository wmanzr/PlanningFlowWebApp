package RUT.PlanningFlow.adapter.out.persistence.repository;

import RUT.PlanningFlow.adapter.out.persistence.entity.ExternalResourceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExternalResourceRepository extends BaseRepository<ExternalResourceEntity, Integer> {
    Page<ExternalResourceEntity> findAllByOrderByNameAscIdAsc(Pageable pageable);
    Page<ExternalResourceEntity> findByNameContainingIgnoreCaseOrderByNameAscIdAsc(String searchTerm, Pageable pageable);
}