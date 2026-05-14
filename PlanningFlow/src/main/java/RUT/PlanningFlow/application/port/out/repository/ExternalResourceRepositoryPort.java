package RUT.PlanningFlow.application.port.out.repository;

import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.domain.model.ExternalResource;

import java.util.List;
import java.util.Optional;

public interface ExternalResourceRepositoryPort {
    Optional<ExternalResource> findById(Integer id);
    List<ExternalResource> findAll();
    PageResult<ExternalResource> findExternalResources(PageQuery pageQuery);
    PageResult<ExternalResource> findByNameContainingIgnoreCase(String searchTerm, PageQuery pageQuery);
    Optional<Integer> create(ExternalResource resource);
    Optional<Integer> update(ExternalResource resource);
}