package RUT.PlanningFlow.adapter.in.web.dto.task;

import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.vo.EventMode;
import RUT.PlanningFlow.domain.vo.MatchingDistance;
import RUT.PlanningFlow.domain.vo.WorkloadPolicy;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;

public class TaskMatchRequest {

    @NotNull(message = "Требуемое число участников обязательно")
    @Min(value = 1, message = "Требуемое число участников должно быть не меньше 1")
    private Integer requiredCount;

    @NotNull(message = "Режим подбора обязателен")
    private MatchingMode matchingMode;

    private Double geoReferenceRadiusMeters;

    private Long maxDailyLoadMinutes;
    private Long minTechnicalGapMinutes;

    public TaskMatchRequest() {
    }

    @AssertTrue(message = "Укажите и суточный лимит, и технический разрыв, или не указывайте ни одного")
    boolean isWorkloadPairConsistent() {
        if (maxDailyLoadMinutes == null && minTechnicalGapMinutes == null) {
            return true;
        }
        return maxDailyLoadMinutes != null && minTechnicalGapMinutes != null;
    }

    public EventMode toEventMode() {
        final double radius = geoReferenceRadiusMeters != null
                ? geoReferenceRadiusMeters
                : MatchingDistance.CITY_SCALE.referenceRadiusMeters();
        final WorkloadPolicy workloadPolicy;
        if (maxDailyLoadMinutes != null && minTechnicalGapMinutes != null) {
            workloadPolicy = new WorkloadPolicy(
                    Duration.ofMinutes(maxDailyLoadMinutes),
                    Duration.ofMinutes(minTechnicalGapMinutes)
            );
        } else {
            workloadPolicy = WorkloadPolicy.defaults();
        }
        return new EventMode(matchingMode, new MatchingDistance(radius), workloadPolicy);
    }

    public Integer getRequiredCount() {
        return requiredCount;
    }

    public void setRequiredCount(final Integer requiredCount) {
        this.requiredCount = requiredCount;
    }

    public MatchingMode getMatchingMode() {
        return matchingMode;
    }

    public void setMatchingMode(final MatchingMode matchingMode) {
        this.matchingMode = matchingMode;
    }

    public Double getGeoReferenceRadiusMeters() {
        return geoReferenceRadiusMeters;
    }
    public void setGeoReferenceRadiusMeters(final Double geoReferenceRadiusMeters) {
        this.geoReferenceRadiusMeters = geoReferenceRadiusMeters;
    }

    public Long getMaxDailyLoadMinutes() {
        return maxDailyLoadMinutes;
    }

    public void setMaxDailyLoadMinutes(final Long maxDailyLoadMinutes) {
        this.maxDailyLoadMinutes = maxDailyLoadMinutes;
    }

    public Long getMinTechnicalGapMinutes() {
        return minTechnicalGapMinutes;
    }

    public void setMinTechnicalGapMinutes(final Long minTechnicalGapMinutes) {
        this.minTechnicalGapMinutes = minTechnicalGapMinutes;
    }
}
