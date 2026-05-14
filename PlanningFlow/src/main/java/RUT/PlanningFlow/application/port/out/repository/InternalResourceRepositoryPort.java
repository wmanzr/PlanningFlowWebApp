package RUT.PlanningFlow.application.port.out.repository;

import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.domain.model.InternalResource;

import java.util.List;
import java.util.Optional;

public interface InternalResourceRepositoryPort {
    Optional<InternalResource> findById(Integer id);
    List<InternalResource> findAll();
    PageResult<InternalResource> findInternalResources(PageQuery pageQuery);
    PageResult<InternalResource> findByNameContainingIgnoreCase(String searchTerm, PageQuery pageQuery);
    Optional<Integer> create(InternalResource resource);
    Optional<Integer> update(InternalResource resource);

    
    boolean deleteById(Integer id);
}