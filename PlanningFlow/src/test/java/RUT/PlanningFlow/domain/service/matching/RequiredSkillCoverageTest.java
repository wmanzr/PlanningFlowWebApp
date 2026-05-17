package RUT.PlanningFlow.domain.service.matching;

import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RequiredSkillCoverageTest {

    @Test
    void userSkillIdsCovering_includes_category_match_when_required_skill_not_exact() {
        final Skill required = new Skill(10, "Driver", "Transport");
        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                Duration.ZERO,
                Map.of(20, 0.8d, 30, 0.5d),
                Map.of("transport", 0.8d, "kitchen", 0.5d),
                Map.of(20, "transport", 30, "kitchen")
        );

        final List<Integer> matched = RequiredSkillCoverage.userSkillIdsCovering(List.of(required), snapshot);

        assertThat(matched).containsExactly(20);
    }

    @Test
    void userSkillIdsCovering_includes_category_match_for_related_user_skill() {
        final Skill required = new Skill(10, "Driver", "Transport");
        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                Duration.ZERO,
                Map.of(20, 0.8d),
                Map.of("transport", 0.8d),
                Map.of(20, "transport")
        );

        final List<Integer> matched = RequiredSkillCoverage.userSkillIdsCovering(List.of(required), snapshot);

        assertThat(matched).containsExactly(20);
    }

    @Test
    void userSkillIdsCovering_includes_exact_skill_id() {
        final Skill required = new Skill(10, "Driver", "Transport");
        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                Duration.ZERO,
                Map.of(10, 1.0d),
                Map.of("transport", 1.0d),
                Map.of(10, "transport")
        );

        final List<Integer> matched = RequiredSkillCoverage.userSkillIdsCovering(List.of(required), snapshot);

        assertThat(matched).containsExactly(10);
    }
}
