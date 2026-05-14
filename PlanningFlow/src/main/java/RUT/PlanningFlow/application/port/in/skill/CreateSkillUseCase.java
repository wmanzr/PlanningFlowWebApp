package RUT.PlanningFlow.application.port.in.skill;

public interface CreateSkillUseCase {
    Integer execute(String name, String category);
}