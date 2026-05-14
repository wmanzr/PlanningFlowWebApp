package RUT.PlanningFlow.adapter.out.persistence.repository;

import RUT.PlanningFlow.adapter.out.persistence.entity.InternalResourceEntity;
import RUT.PlanningFlow.domain.enums.ResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InternalResourceRepository extends BaseRepository<InternalResourceEntity, Integer> {
    Page<InternalResourceEntity> findAllByOrderByNameAscIdAsc(Pageable pageable);
    Page<InternalResourceEntity> findByNameContainingIgnoreCaseOrderByNameAscIdAsc(String searchTerm, Pageable pageable);
    List<InternalResourceEntity> findByTypeAndOperationalTrueAndNameIgnoreCase(ResourceType type, String name);
}