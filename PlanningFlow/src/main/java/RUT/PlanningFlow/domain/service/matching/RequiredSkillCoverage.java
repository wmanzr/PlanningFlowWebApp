package RUT.PlanningFlow.domain.service.matching;

import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RequiredSkillCoverage {

    private RequiredSkillCoverage() {
    }

    public static List<Integer> userSkillIdsCovering(
            final List<Skill> requiredSkills,
            final CandidateSnapshot snapshot
    ) {
        if (requiredSkills == null || requiredSkills.isEmpty() || snapshot == null) {
            return List.of();
        }
        final Map<Integer, Double> userSkills = snapshot.exactSkillWeights();
        if (userSkills.isEmpty()) {
            return List.of();
        }
        final Map<Integer, String> categories = snapshot.skillIdToCategory();
        final Set<Integer> matched = new LinkedHashSet<>();

        for (final Skill required : requiredSkills) {
            if (required == null) {
                continue;
            }
            final Integer requiredId = required.getId();
            if (requiredId != null && userSkills.containsKey(requiredId)) {
                matched.add(requiredId);
            }
        }

        for (final Skill required : requiredSkills) {
            if (required == null) {
                continue;
            }
            final String requiredCategory = CandidateSnapshot.normalizedCategory(required);
            if (requiredCategory.isEmpty()) {
                continue;
            }
            for (final Integer userSkillId : userSkills.keySet()) {
                if (userSkillId == null) {
                    continue;
                }
                final String userCategory = categories.getOrDefault(userSkillId, "");
                if (requiredCategory.equals(userCategory)) {
                    matched.add(userSkillId);
                }
            }
        }

        return List.copyOf(matched);
    }
}