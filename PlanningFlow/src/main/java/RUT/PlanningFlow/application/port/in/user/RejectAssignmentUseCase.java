package RUT.PlanningFlow.application.port.in.user;

public interface RejectAssignmentUseCase {
    void execute(Integer assignmentId, String reason);
}