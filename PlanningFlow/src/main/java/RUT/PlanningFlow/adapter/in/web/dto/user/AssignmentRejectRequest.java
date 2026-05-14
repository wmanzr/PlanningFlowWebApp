package RUT.PlanningFlow.adapter.in.web.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AssignmentRejectRequest {

    @NotBlank(message = "Причина отказа обязательна")
    @Size(max = 8192, message = "Причина слишком длинная")
    private String reason;

    public AssignmentRejectRequest() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }
}