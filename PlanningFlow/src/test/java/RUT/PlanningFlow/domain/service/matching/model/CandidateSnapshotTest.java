package RUT.PlanningFlow.domain.service.matching.model;

import RUT.PlanningFlow.domain.enums.SkillTier;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.model.UserSkill;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateSnapshotTest {

    @Test
    void normalized_category_trims_and_lowercases() {
        assertThat(CandidateSnapshot.normalizedCategory("  Medical ")).isEqualTo("medical");
        assertThat(CandidateSnapshot.normalizedCategory((String) null)).isEmpty();
    }

    @Test
    void normalized_category_from_skill_delegates() {
        final Skill skill = new Skill(1, "x", "Transport");

        assertThat(CandidateSnapshot.normalizedCategory(skill)).isEqualTo("transport");
    }

    @Test
    void normalized_category_blank_string_is_empty() {
        assertThat(CandidateSnapshot.normalizedCategory("   ")).isEmpty();
    }

    @Test
    void normalized_category_from_null_skill_is_empty() {
        assertThat(CandidateSnapshot.normalizedCategory((Skill) null)).isEmpty();
    }

    @Test
    void empty_snapshot_has_empty_maps() {
        final CandidateSnapshot empty = CandidateSnapshot.empty();

        assertThat(empty.exactSkillWeights()).isEmpty();
        assertThat(empty.cumulativeCategoryWeights()).isEmpty();
    }

    @Test
    void create_merges_skills_and_skips_null_entries() {
        final User user = DomainFixtures.user(1);
        final Skill skill = DomainFixtures.skill(4, "Lift", "Safety");
        final UserSkill novice = new UserSkill(1, user, skill, SkillTier.NOVICE, null);
        final UserSkill expert = new UserSkill(2, user, skill, SkillTier.EXPERT, null);

        final CandidateSnapshot snapshot = CandidateSnapshot.create(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                Duration.ZERO,
                Arrays.asList(null, novice, expert)
        );

        assertThat(snapshot.exactSkillWeights().get(4)).isEqualTo(1.0);
        assertThat(snapshot.cumulativeCategoryWeights().get("safety")).isEqualTo(1.0);
    }

    @Test
    void create_skips_exact_weight_when_skill_id_absent_but_keeps_category() {
        final Skill catalogSkillWithoutId = new Skill(null, "Lift", "Safety");
        final UserSkill us = new UserSkill(
                1,
                DomainFixtures.user(1),
                catalogSkillWithoutId,
                SkillTier.EXPERT,
                null
        );

        final CandidateSnapshot snapshot = CandidateSnapshot.create(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                Duration.ZERO,
                List.of(us)
        );

        assertThat(snapshot.exactSkillWeights()).isEmpty();
        assertThat(snapshot.cumulativeCategoryWeights().get("safety")).isEqualTo(1.0);
    }

    @Test
    void record_compact_ctor_replaces_null_maps_with_empty() {
        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                null,
                null,
                null,
                Map.of());

        assertThat(snapshot.exactSkillWeights()).isEmpty();
        assertThat(snapshot.cumulativeCategoryWeights()).isEmpty();
        assertThat(snapshot.skillIdToCategory()).isEmpty();
    }

    @Test
    void create_populates_skill_id_to_category() {
        final User user = DomainFixtures.user(1);
        final Skill skill = DomainFixtures.skill(4, "Lift", "Safety");
        final UserSkill us = new UserSkill(1, user, skill, SkillTier.EXPERT, null);

        final CandidateSnapshot snapshot = CandidateSnapshot.create(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                Duration.ZERO,
                List.of(us)
        );

        assertThat(snapshot.skillIdToCategory()).containsEntry(4, "safety");
    }
}
