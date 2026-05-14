package RUT.PlanningFlow.domain.service.matching.selection;

import RUT.PlanningFlow.domain.service.matching.model.RankedCandidate;
import RUT.PlanningFlow.domain.utils.DomainAssert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DefaultSelection implements FinalSelection {
    @Override
    public List<RankedCandidate> select(final List<RankedCandidate> candidates) {
        DomainAssert.notNull(candidates, "Список кандидатов для финального выбора обязателен", "FINAL_SELECTION_CANDIDATES_REQUIRED");
        final List<RankedCandidate> ordered = new ArrayList<>(candidates);
        ordered.sort(comparator());
        return List.copyOf(ordered);
    }

    private static Comparator<RankedCandidate> comparator() {
        return Comparator
                .comparingDouble((RankedCandidate rc) -> rc.score().totalScore()).reversed()
                .thenComparingDouble((RankedCandidate rc) -> rc.score().skillScore()).reversed()
                .thenComparingDouble((RankedCandidate rc) -> rc.score().geoScore()).reversed()
                .thenComparing(
                        rc -> rc.candidate().getId(),
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
                .thenComparing(rc -> rc.candidate().getUsername(), Comparator.nullsLast(String::compareTo));
    }
}