package RUT.PlanningFlow.application.port.in.event;

public interface GeneratePostMortemAIReportUseCase {
    void execute(Integer callerUserId, Integer eventId);
}
