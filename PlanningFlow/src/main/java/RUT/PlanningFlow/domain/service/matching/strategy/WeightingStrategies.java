package RUT.PlanningFlow.domain.service.matching.strategy;

import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.utils.DomainAssert;

public final class WeightingStrategies {

    private static final WeightingStrategy STANDARD = new BalancedStrategy();
    private static final WeightingStrategy CRITICAL = new CriticalStrategy();

    private WeightingStrategies() {
    }

    public static WeightingStrategy forMode(final MatchingMode mode) {
        DomainAssert.notNull(mode, "Режим подбора обязателен", "MATCHING_MODE_REQUIRED");
        return switch (mode) {
            case STANDARD -> STANDARD;
            case CRITICAL -> CRITICAL;
        };
    }
}