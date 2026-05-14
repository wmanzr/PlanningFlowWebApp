package RUT.PlanningFlow.adapter.in.web.dto.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class IncidentResolveRequest {

    @NotBlank(message = "Заметки по разрешению обязательны")
    @Size(max = 65535, message = "Текст слишком длинный")
    private String resolutionNotes;

    public IncidentResolveRequest() {
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(final String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}
