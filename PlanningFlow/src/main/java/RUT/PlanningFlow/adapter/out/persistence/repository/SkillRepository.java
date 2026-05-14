package RUT.PlanningFlow.adapter.out.persistence.repository;

import RUT.PlanningFlow.adapter.out.persistence.entity.SkillEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SkillRepository extends BaseRepository<SkillEntity, Integer> {
    boolean existsByNameIgnoreCase(String name);
    Page<SkillEntity> findAllByOrderByNameAscIdAsc(Pageable pageable);
    @Query("SELECT s FROM SkillEntity s ORDER BY s.category ASC, s.name ASC, s.id ASC")
    Page<SkillEntity> findCatalogOrderedByCategoryThenName(Pageable pageable);
    Page<SkillEntity> findByNameContainingIgnoreCaseOrderByNameAscIdAsc(String searchTerm, Pageable pageable);
    @Query("SELECT DISTINCT s.category FROM SkillEntity s ORDER BY s.category ASC")
    List<String> findDistinctCategoriesOrderByCategoryAsc();

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM task_required_skills WHERE skill_id = :skillId", nativeQuery = true)
    void deleteTaskRequiredSkillLinksBySkillId(@Param("skillId") Integer skillId);
}