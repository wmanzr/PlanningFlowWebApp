package RUT.PlanningFlow.application.service.skill;

import RUT.PlanningFlow.application.port.in.skill.DeleteSkillUseCase;
import RUT.PlanningFlow.application.port.out.repository.SkillRepositoryPort;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteSkillService implements DeleteSkillUseCase {

    private final SkillRepositoryPort skillRepository;

    public DeleteSkillService(final SkillRepositoryPort skillRepository) {
        DomainAssert.notNull(skillRepository, "Репозиторий навыков обязателен", "SKILL_REPOSITORY_REQUIRED");
        this.skillRepository = skillRepository;
    }

    @Override
    public boolean execute(final Integer skillId) {
        return skillRepository.deleteCatalogEntry(skillId);
    }
}
