package RUT.PlanningFlow.domain.service.matching.strategy;

public final class CriticalStrategy implements WeightingStrategy {

    private static final double SKILL = 0.30d;
    private static final double GEO = 0.6d;
    private static final double LOAD = 0.1d;

    @Override
    public String name() {
        return "CRITICAL";
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