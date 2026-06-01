package RUT.PlanningFlow.application.service.skill;

import RUT.PlanningFlow.application.dto.skill.SkillResponseDto;
import RUT.PlanningFlow.application.port.in.skill.GetSkillDetailsQuery;
import RUT.PlanningFlow.application.port.out.repository.SkillRepositoryPort;
import RUT.PlanningFlow.config.cache.ApplicationRedisCacheConfiguration;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GetSkillDetailsService implements GetSkillDetailsQuery {

    private final SkillRepositoryPort skillRepository;
    private final SkillResponseDtoMapper skillResponseDtoMapper;

    public GetSkillDetailsService(
            final SkillRepositoryPort skillRepository,
            final SkillResponseDtoMapper skillResponseDtoMapper
    ) {
        DomainAssert.notNull(skillRepository, "Репозиторий навыков обязателен", "SKILL_REPOSITORY_REQUIRED");
        DomainAssert.notNull(skillResponseDtoMapper, "Маппер ответа по навыку обязателен", "SKILL_RESPONSE_DTO_MAPPER_REQUIRED");
        this.skillRepository = skillRepository;
        this.skillResponseDtoMapper = skillResponseDtoMapper;
    }

    @Override
    @Cacheable(
            cacheNames = ApplicationRedisCacheConfiguration.CACHE_SKILL_DETAILS,
            key = "#skillId",
            unless = "#result == null || (#result instanceof T(java.util.Optional) && !#result.isPresent())"
    )
    public Optional<SkillResponseDto> execute(final Integer skillId) {
        DomainAssert.notNull(skillId, "ID навыка обязателен", "SKILL_ID_REQUIRED");
        return skillRepository.findById(skillId).map(skillResponseDtoMapper::toResponse);
    }
}
