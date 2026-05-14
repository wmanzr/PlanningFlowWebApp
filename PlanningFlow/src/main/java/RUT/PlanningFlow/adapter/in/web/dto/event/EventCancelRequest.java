package RUT.PlanningFlow.adapter.in.web.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EventCancelRequest {

    @NotBlank(message = "Причина отмены обязательна")
    @Size(max = 8192, message = "Причина отмены слишком длинная")
    private String reason;

    public EventCancelRequest() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }
}
