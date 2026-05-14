package RUT.PlanningFlow.application.port.in.task;

public interface UnassignParticipantUseCase {
    void execute(Integer callerUserId, Integer taskId, Integer userId);
}
