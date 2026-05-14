package RUT.PlanningFlow.domain.service.matching.filter;

import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.service.matching.model.ScoreBreakdown;

public interface CandidatePhaseBFilter {
    ScoreBreakdown score(Task task, User candidate, MatchingContext context);
}