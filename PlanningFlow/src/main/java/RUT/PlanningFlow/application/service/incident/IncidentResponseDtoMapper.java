package RUT.PlanningFlow.application.service.incident;

import RUT.PlanningFlow.application.dto.incident.IncidentResponseDto;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Incident;
import RUT.PlanningFlow.domain.model.Resource;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public final class IncidentResponseDtoMapper {

    public IncidentResponseDto toResponse(final Incident incident) {
        if (incident == null) {
            return null;
        }

        final Event event = incident.getEvent();
        final Task task = incident.getTask();
        final Resource resource = incident.getResource();
        final User reporter = incident.getReporter();

        return new IncidentResponseDto(
                incident.getId(),
                event == null ? null : event.getId(),
                task == null ? null : task.getId(),
                resource == null ? null : resource.getId(),
                reporter == null ? null : reporter.getId(),
                incident.getDescription(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getCreatedAt(),
                incident.getResolvedAt(),
                incident.getResolutionNotes()
        );
    }
}
