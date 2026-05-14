package RUT.PlanningFlow.application.dto.skill;

import RUT.PlanningFlow.domain.model.Skill;

public final class SkillResponseDto {
    private final Integer id;
    private final String name;
    private final String category;

    public SkillResponseDto(final Integer id, final String name, final String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public static SkillResponseDto from(final Skill skill) {
        if (skill == null) {
            return null;
        }
        return new SkillResponseDto(skill.getId(), skill.getName(), skill.getCategory());
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
}