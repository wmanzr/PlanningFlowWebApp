package RUT.PlanningFlow.application.service.skill;

import RUT.PlanningFlow.application.dto.skill.SkillResponseDto;
import RUT.PlanningFlow.application.port.in.skill.GetSkillDetailsQuery;
import RUT.PlanningFlow.application.port.out.repository.SkillRepositoryPort;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GetSkillDetailsService implements GetSkillDetailsQuery {

    private final SkillRepositoryPort skillRepository;

    public GetSkillDetailsService(final SkillRepositoryPort skillRepository) {
        DomainAssert.notNull(skillRepository, "Репозиторий навыков обязателен", "SKILL_REPOSITORY_REQUIRED");
        this.skillRepository = skillRepository;
    }

    @Override
    public Optional<SkillResponseDto> execute(final Integer skillId) {
        DomainAssert.notNull(skillId, "ID навыка обязателен", "SKILL_ID_REQUIRED");
        return skillRepository.findById(skillId).map(SkillResponseDto::from);
    }
}
