package RUT.PlanningFlow.application.service.skill;

import RUT.PlanningFlow.application.port.in.skill.CreateSkillUseCase;
import RUT.PlanningFlow.application.port.out.repository.SkillRepositoryPort;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateSkillService implements CreateSkillUseCase {

    private final SkillRepositoryPort skillRepository;

    public CreateSkillService(final SkillRepositoryPort skillRepository) {
        DomainAssert.notNull(skillRepository, "Репозиторий навыков обязателен", "SKILL_REPOSITORY_REQUIRED");
        this.skillRepository = skillRepository;
    }

    @Override
    public Integer execute(final String name, final String category) {
        final Skill skill = new Skill(null, name, category);
        Skill.assertCatalogNameNotTaken(skillRepository.existsByNameIgnoreCase(skill.getName()));
        return skillRepository.create(skill)
                .orElseThrow(() -> new DomainException("Не удалось создать навык", "SKILL_CREATE_FAILED"));
    }
}