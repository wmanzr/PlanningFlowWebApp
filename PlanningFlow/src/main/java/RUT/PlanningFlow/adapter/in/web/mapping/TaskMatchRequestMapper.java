package RUT.PlanningFlow.adapter.in.web.mapping;

import RUT.PlanningFlow.adapter.in.web.dto.task.TaskMatchRequest;
import RUT.PlanningFlow.domain.vo.EventMode;
import RUT.PlanningFlow.domain.vo.MatchingDistance;
import RUT.PlanningFlow.domain.vo.WorkloadPolicy;

import java.time.Duration;

public final class TaskMatchRequestMapper {

    private TaskMatchRequestMapper() {
    }

    public static EventMode toEventMode(final TaskMatchRequest request) {
        final double radius = request.getGeoReferenceRadiusMeters() != null
                ? request.getGeoReferenceRadiusMeters()
                : MatchingDistance.CITY_SCALE.referenceRadiusMeters();
        final WorkloadPolicy workloadPolicy;
        if (request.getMaxDailyLoadMinutes() != null && request.getMinTechnicalGapMinutes() != null) {
            workloadPolicy = new WorkloadPolicy(
                    Duration.ofMinutes(request.getMaxDailyLoadMinutes()),
                    Duration.ofMinutes(request.getMinTechnicalGapMinutes())
            );
        } else {
            workloadPolicy = WorkloadPolicy.defaults();
        }
        return new EventMode(request.getMatchingMode(), new MatchingDistance(radius), workloadPolicy);
    }
}
