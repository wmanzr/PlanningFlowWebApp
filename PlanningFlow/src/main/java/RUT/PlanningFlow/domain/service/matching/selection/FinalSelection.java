package RUT.PlanningFlow.domain.service.matching.selection;

import RUT.PlanningFlow.domain.service.matching.model.RankedCandidate;

import java.util.List;

public interface FinalSelection {
    List<RankedCandidate> select(List<RankedCandidate> candidates);
}