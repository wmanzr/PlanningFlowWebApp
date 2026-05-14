package RUT.PlanningFlow.application.port.in.incident;

public interface ResolveIncidentUseCase {
    void execute(Integer incidentId, String resolutionNotes);
}