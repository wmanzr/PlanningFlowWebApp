package RUT.PlanningFlow.domain.vo;

import RUT.PlanningFlow.domain.utils.DomainAssert;

import java.time.Duration;

public record WorkloadPolicy(
        Duration maxDailyLoad,
        Duration minTechnicalGap
) {
    public WorkloadPolicy {
        DomainAssert.notNull(maxDailyLoad, "Суточный лимит нагрузки обязателен", "MAX_DAILY_LOAD_REQUIRED");
        DomainAssert.notNull(minTechnicalGap, "Минимальный технический разрыв обязателен", "MIN_TECHNICAL_GAP_REQUIRED");
        DomainAssert.isTrue(!maxDailyLoad.isNegative(), "Суточный лимит не может быть отрицательным", "INVALID_MAX_DAILY_LOAD");
        DomainAssert.isTrue(!minTechnicalGap.isNegative(), "Технический разрыв не может быть отрицательным", "INVALID_MIN_TECHNICAL_GAP");
    }

    public static WorkloadPolicy defaults() {
        return new WorkloadPolicy(
                Duration.ofHours(8),
                Duration.ofMinutes(15)
        );
    }
}