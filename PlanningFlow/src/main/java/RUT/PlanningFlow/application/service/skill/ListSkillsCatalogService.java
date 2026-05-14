package RUT.PlanningFlow.application.service.skill;

import RUT.PlanningFlow.application.dto.skill.SkillResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.skill.ListSkillsCatalogQuery;
import RUT.PlanningFlow.application.port.out.repository.SkillRepositoryPort;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListSkillsCatalogService implements ListSkillsCatalogQuery {

    private final SkillRepositoryPort skillRepository;

    public ListSkillsCatalogService(final SkillRepositoryPort skillRepository) {
        DomainAssert.notNull(skillRepository, "Репозиторий навыков обязателен", "SKILL_REPOSITORY_REQUIRED");
        this.skillRepository = skillRepository;
    }

    @Override
    public PageResult<SkillResponseDto> execute(final String name, final PageQuery pageQuery) {
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");

        final String term = name == null || name.isBlank() ? null : name.trim();
        final PageResult<Skill> page = term == null
                ? skillRepository.findSkillsOrderByCategoryAscNameAsc(pageQuery)
                : skillRepository.findByNameContainingIgnoreCase(term, pageQuery);

        final List<SkillResponseDto> items = new ArrayList<>(page.items().size());
        for (final Skill s : page.items()) {
            items.add(SkillResponseDto.from(s));
        }
        return new PageResult<>(items, page.totalElements(), page.totalPages());
    }
}