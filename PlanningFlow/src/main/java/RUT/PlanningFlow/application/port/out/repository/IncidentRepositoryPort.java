package RUT.PlanningFlow.application.port.out.repository;

import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import RUT.PlanningFlow.domain.model.Incident;

import java.util.List;
import java.util.Optional;

public interface IncidentRepositoryPort {
    Optional<Incident> findById(Integer id);

    
    List<Incident> findOpenOrInProgressByEventId(Integer eventId);

    List<Incident> findAll();
    PageResult<Incident> findIncidents(PageQuery pageQuery);
    PageResult<Incident> findByEventId(Integer eventId, PageQuery pageQuery);
    PageResult<Incident> findByDescriptionContainingIgnoreCase(String searchTerm, PageQuery pageQuery);

    long countByStatus(IncidentStatus status);

    Optional<Integer> create(Incident incident);
    Optional<Integer> update(Incident incident);
}