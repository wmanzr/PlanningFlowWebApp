package RUT.PlanningFlow.application.port.in.task;

public interface AssignParticipantUseCase {
    Integer execute(Integer callerUserId, Integer taskId, Integer userId);
}
