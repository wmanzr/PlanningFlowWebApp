package RUT.PlanningFlow.adapter.in.web.dto.skill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SkillCreateRequest {

    @NotBlank(message = "Название навыка обязательно")
    @Size(max = 512, message = "Название слишком длинное")
    private String name;

    @NotBlank(message = "Категория обязательна")
    @Size(max = 512, message = "Категория слишком длинная")
    private String category;

    public SkillCreateRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }
}
