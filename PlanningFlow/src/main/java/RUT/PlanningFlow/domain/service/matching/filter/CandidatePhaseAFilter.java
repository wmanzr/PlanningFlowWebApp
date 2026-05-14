package RUT.PlanningFlow.domain.service.matching.filter;

import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.service.matching.model.RejectedCandidate;

public interface CandidatePhaseAFilter {
    RejectedCandidate rejectOrNull(Task task, User candidate, MatchingContext context);
}