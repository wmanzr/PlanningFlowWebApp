package RUT.PlanningFlow.domain.service.matching.model;

import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.UserSkill;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import RUT.PlanningFlow.domain.vo.ScheduleInterval;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record CandidateSnapshot(
        LocalDateTime occupiedUntil,
        List<ScheduleInterval> committedIntervals,
        LocalDateTime previousTaskEndedAt,
        GeoPoint previousTaskLocation,
        Duration previousTaskDuration,
        Duration workedToday,
        Map<Integer, Double> exactSkillWeights,
        Map<String, Double> cumulativeCategoryWeights
) {
    public CandidateSnapshot {
        committedIntervals = committedIntervals == null ? List.of() : List.copyOf(committedIntervals);
        exactSkillWeights = exactSkillWeights == null ? Map.of() : Map.copyOf(exactSkillWeights);
        cumulativeCategoryWeights = cumulativeCategoryWeights == null ? Map.of() : Map.copyOf(cumulativeCategoryWeights);
    }

    public static CandidateSnapshot empty() {
        return new CandidateSnapshot(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                Duration.ZERO,
                Map.of(),
                Map.of()
        );
    }

    public static CandidateSnapshot create(final LocalDateTime occupiedUntil,
            final List<ScheduleInterval> committedIntervals,
            final LocalDateTime previousTaskEndedAt, final GeoPoint previousTaskLocation,
            final Duration previousTaskDuration, final Duration workedToday,
            final List<UserSkill> rawSkills
    ) {
        final Map<Integer, Double> exact = new HashMap<>();
        final Map<String, Double> maxByCategory = new HashMap<>();

        if (rawSkills != null) {
            for (final UserSkill us : rawSkills) {
                if (us == null || us.getSkill() == null) {
                    continue;
                }
                final Skill skill = us.getSkill();
                final double w = Math.clamp(us.getTier().getWeight(), 0.0d, 1.0d);
                final Integer skillId = skill.getId();
                if (skillId != null) {
                    exact.merge(skillId, w, Math::max);
                }
                final String category = normalizedCategory(skill.getCategory());
                maxByCategory.merge(category, w, Math::max);
            }
        }

        final Map<String, Double> cumulative = new HashMap<>();
        for (final Map.Entry<String, Double> e : maxByCategory.entrySet()) {
            cumulative.put(e.getKey(), e.getValue());
        }

        return new CandidateSnapshot(
                occupiedUntil,
                committedIntervals,
                previousTaskEndedAt,
                previousTaskLocation,
                previousTaskDuration,
                workedToday,
                Map.copyOf(exact),
                Map.copyOf(cumulative)
        );
    }

    public static String normalizedCategory(final String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return raw.trim().toLowerCase();
    }

    public static String normalizedCategory(final Skill skill) {
        return skill == null ? "" : normalizedCategory(skill.getCategory());
    }
}