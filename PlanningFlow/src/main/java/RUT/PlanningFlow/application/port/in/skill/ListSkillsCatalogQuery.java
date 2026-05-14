package RUT.PlanningFlow.application.port.in.skill;

import RUT.PlanningFlow.application.dto.skill.SkillResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;

public interface ListSkillsCatalogQuery {
    PageResult<SkillResponseDto> execute(String name, PageQuery pageQuery);
}