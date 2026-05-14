package RUT.PlanningFlow.adapter.out.persistence.repository;

import RUT.PlanningFlow.adapter.out.persistence.entity.UserSkillEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserSkillRepository extends BaseRepository<UserSkillEntity, Integer> {
    Page<UserSkillEntity> findAllByOrderByVerifiedAtDescIdDesc(Pageable pageable);
    Page<UserSkillEntity> findBySkill_NameContainingIgnoreCaseOrderByVerifiedAtDescIdDesc(String searchTerm, Pageable pageable);
    List<UserSkillEntity> findByUser_IdOrderByVerifiedAtDescIdDesc(Integer userId);
    Optional<UserSkillEntity> findByUser_IdAndSkill_Id(Integer userId, Integer skillId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserSkillEntity us WHERE us.id = :id")
    void deleteById(@Param("id") Integer id);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserSkillEntity us WHERE us.skill.id = :skillId")
    void deleteAllForSkillId(@Param("skillId") Integer skillId);
}