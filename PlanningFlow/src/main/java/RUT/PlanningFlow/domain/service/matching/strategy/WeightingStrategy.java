package RUT.PlanningFlow.domain.service.matching.strategy;

public interface WeightingStrategy {
    String name();
    double getSkillWeight();
    double getGeoWeight();
    double getLoadWeight();
}