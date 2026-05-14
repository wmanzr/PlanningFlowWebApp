package RUT.PlanningFlow.application.service.skill;

import RUT.PlanningFlow.application.port.in.skill.ListSkillCategoriesQuery;
import RUT.PlanningFlow.application.port.out.repository.SkillRepositoryPort;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListSkillCategoriesService implements ListSkillCategoriesQuery {

    private final SkillRepositoryPort skillRepository;

    public ListSkillCategoriesService(final SkillRepositoryPort skillRepository) {
        DomainAssert.notNull(skillRepository, "Репозиторий навыков обязателен", "SKILL_REPOSITORY_REQUIRED");
        this.skillRepository = skillRepository;
    }

    @Override
    public List<String> execute() {
        return skillRepository.findDistinctCategories();
    }
}