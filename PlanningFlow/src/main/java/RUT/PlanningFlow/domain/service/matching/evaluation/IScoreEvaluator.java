package RUT.PlanningFlow.domain.service.matching.evaluation;

import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;

public interface IScoreEvaluator {
    double evaluate(Task task, CandidateSnapshot snapshot, MatchingContext context);
}