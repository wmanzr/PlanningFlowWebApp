package RUT.PlanningFlow.application.port.out.repository;

import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.domain.model.UserSkill;

import java.util.List;
import java.util.Optional;

public interface UserSkillRepositoryPort {
    Optional<UserSkill> findById(Integer id);
    List<UserSkill> findAll();
    PageResult<UserSkill> findUserSkills(PageQuery pageQuery);
    PageResult<UserSkill> findBySkillNameContainingIgnoreCase(String searchTerm, PageQuery pageQuery);
    List<UserSkill> findForUser(Integer userId);
    Optional<UserSkill> findByUserIdAndSkillId(Integer userId, Integer skillId);
    Optional<Integer> create(UserSkill userSkill);
    Optional<Integer> update(UserSkill userSkill);

    void deleteById(Integer id);
}