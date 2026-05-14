package RUT.PlanningFlow.domain.service.matching.model;

import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;

public record RankedCandidate(
        User candidate,
        ScoreBreakdown score,
        int rank
) {
    public RankedCandidate {
        DomainAssert.notNull(candidate, "Кандидат обязателен", "MATCHING_CANDIDATE_REQUIRED");
        DomainAssert.notNull(score, "Результат скоринга обязателен", "MATCHING_SCORE_REQUIRED");
    }
}