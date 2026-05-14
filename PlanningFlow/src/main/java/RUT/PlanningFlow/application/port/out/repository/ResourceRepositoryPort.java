package RUT.PlanningFlow.application.port.out.repository;

import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.domain.model.Resource;

import java.util.List;
import java.util.Optional;

public interface ResourceRepositoryPort {
    Optional<Resource> findById(Integer id);
    List<Resource> findAll();
    PageResult<Resource> findResources(PageQuery pageQuery);
    PageResult<Resource> findByNameContainingIgnoreCase(String searchTerm, PageQuery pageQuery);
    Optional<Integer> create(Resource resource);
    Optional<Integer> update(Resource resource);
}