package RUT.PlanningFlow.domain.service.matching.model;

import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;

public record RejectedCandidate(
        User candidate,
        RejectionReason reason,
        String details
) {
    public RejectedCandidate {
        DomainAssert.notNull(candidate, "Кандидат обязателен", "MATCHING_CANDIDATE_REQUIRED");
        DomainAssert.notNull(reason, "Причина отклонения обязательна", "MATCHING_REJECTION_REASON_REQUIRED");
    }
}
