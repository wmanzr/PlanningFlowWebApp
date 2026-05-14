package RUT.PlanningFlow.adapter.out.persistence.repository;

import RUT.PlanningFlow.adapter.out.persistence.entity.IncidentEntity;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IncidentRepository extends BaseRepository<IncidentEntity, Integer> {

    long countByStatus(IncidentStatus status);

    Page<IncidentEntity> findAllByOrderByIdDesc(Pageable pageable);
    Page<IncidentEntity> findByEvent_IdOrderByCreatedAtDescIdDesc(Integer eventId, Pageable pageable);

    List<IncidentEntity> findByEvent_IdAndStatusInOrderByIdAsc(Integer eventId, List<IncidentStatus> statuses);
    Page<IncidentEntity> findByDescriptionContainingIgnoreCaseOrderByIdDesc(String searchTerm, Pageable pageable);
}