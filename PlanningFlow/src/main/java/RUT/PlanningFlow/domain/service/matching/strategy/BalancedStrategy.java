package RUT.PlanningFlow.domain.service.matching.strategy;

public final class BalancedStrategy implements WeightingStrategy {

    private static final double SKILL = 0.40d;
    private static final double GEO = 0.3d;
    private static final double LOAD = 0.3d;

    @Override
    public String name() {
        return "BALANCED";
    }

    @Override
    public double getSkillWeight() {
        return SKILL;
    }

    @Override
    public double getGeoWeight() {
        return GEO;
    }

    @Override
    public double getLoadWeight() {
        return LOAD;
    }
}