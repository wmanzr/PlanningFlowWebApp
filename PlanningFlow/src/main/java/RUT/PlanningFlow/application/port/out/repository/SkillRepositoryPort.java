package RUT.PlanningFlow.application.port.out.repository;

import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.domain.model.Skill;

import java.util.List;
import java.util.Optional;

public interface SkillRepositoryPort {
    Optional<Skill> findById(Integer id);
    List<Skill> findAll();
    List<String> findDistinctCategories();
    boolean existsByNameIgnoreCase(String name);
    PageResult<Skill> findSkills(PageQuery pageQuery);
    PageResult<Skill> findSkillsOrderByCategoryAscNameAsc(PageQuery pageQuery);
    PageResult<Skill> findByNameContainingIgnoreCase(String searchTerm, PageQuery pageQuery);
    Optional<Integer> create(Skill skill);
    Optional<Integer> update(Skill skill);

    
    boolean deleteCatalogEntry(Integer skillId);
}