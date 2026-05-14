package RUT.PlanningFlow.adapter.in.web.dto.task;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TaskAssignRequest {

    @NotNull(message = "ID участника обязателен")
    @Positive(message = "Идентификатор участника должен быть положительным")
    private Integer userId;

    public TaskAssignRequest() {
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(final Integer userId) {
        this.userId = userId;
    }
}
