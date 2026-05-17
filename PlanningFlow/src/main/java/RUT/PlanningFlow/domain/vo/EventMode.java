package RUT.PlanningFlow.domain.vo;

import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.utils.DomainAssert;

public record EventMode(
        MatchingMode matchingMode,
        MatchingDistance maxGeographicDistance,
        WorkloadPolicy workloadPolicy
) {
    public EventMode {
        DomainAssert.notNull(matchingMode, "Режим подбора обязателен", "MATCHING_MODE_REQUIRED");
        DomainAssert.notNull(maxGeographicDistance, "Масштаб гео-подбора обязателен", "MATCHING_GEO_SCALE_REQUIRED");
        DomainAssert.notNull(workloadPolicy, "Политика нагрузки обязательна", "MATCHING_WORKLOAD_POLICY_REQUIRED");
    }
}