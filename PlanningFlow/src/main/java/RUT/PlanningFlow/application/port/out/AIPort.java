package RUT.PlanningFlow.application.port.out;

public interface AIPort {
    String complete(String systemPrompt, String userContent);
}