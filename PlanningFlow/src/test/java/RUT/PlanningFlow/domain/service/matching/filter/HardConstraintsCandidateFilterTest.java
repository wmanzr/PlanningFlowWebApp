package RUT.PlanningFlow.domain.service.matching.filter;

import RUT.PlanningFlow.domain.enums.MatchingMode;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.service.matching.model.CandidateSnapshot;
import RUT.PlanningFlow.domain.service.matching.model.MatchingContext;
import RUT.PlanningFlow.domain.service.matching.model.RejectedCandidate;
import RUT.PlanningFlow.domain.service.matching.model.RejectionReason;
import RUT.PlanningFlow.domain.support.DomainFixtures;
import RUT.PlanningFlow.domain.vo.EventMode;
import RUT.PlanningFlow.domain.vo.MatchingDistance;
import RUT.PlanningFlow.domain.vo.ScheduleInterval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HardConstraintsCandidateFilterTest {

    private static final LocalDateTime T0 = DomainFixtures.EVENT_RANGE_START.plusHours(4);
    private static final LocalDateTime T1 = T0.plusHours(2);

    private HardConstraintsCandidateFilter filter;

    @BeforeEach
    void setUp() {
        filter = new HardConstraintsCandidateFilter();
    }

    @Test
    void rejects_when_already_assigned_on_this_task() {
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final User candidate = DomainFixtures.user(20);
        final MatchingContext context = DomainFixtures.matchingContext(
                T0.minusHours(1),
                DomainFixtures.defaultEventMode(),
                Map.of(20, CandidateSnapshot.empty()),
                Set.of(20)
        );

        final RejectedCandidate rejected = filter.rejectOrNull(task, candidate, context);

        assertThat(rejected).isNotNull();
        assertThat(rejected.reason()).isEqualTo(RejectionReason.TIME_CONFLICT);
    }

    @Test
    void rejects_when_technical_gap_blocks_start_time() {
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final User candidate = DomainFixtures.user(20);
        final Duration gap = Duration.ofMinutes(30);
        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(new ScheduleInterval(T0.minusHours(2), T0.minusMinutes(10))),
                null,
                null,
                Duration.ZERO,
                Duration.ZERO,
                Map.of(),
                Map.of(),
                Map.of());
        final EventMode mode = DomainFixtures.eventMode(
                MatchingMode.STANDARD,
                MatchingDistance.BUILDING_SCALE,
                DomainFixtures.workloadPolicy(Duration.ofHours(8), gap)
        );
        final MatchingContext context = DomainFixtures.matchingContext(T0.minusHours(1), mode, Map.of(20, snapshot));

        final RejectedCandidate rejected = filter.rejectOrNull(task, candidate, context);

        assertThat(rejected).isNotNull();
        assertThat(rejected.reason()).isEqualTo(RejectionReason.TIME_CONFLICT);
    }

    @Test
    void rejects_when_schedule_overlaps_committed_assignment() {
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final User candidate = DomainFixtures.user(20);
        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(new ScheduleInterval(T0.plusMinutes(30), T1.plusMinutes(30))),
                null,
                null,
                Duration.ZERO,
                Duration.ZERO,
                Map.of(),
                Map.of(),
                Map.of());
        final MatchingContext context = DomainFixtures.matchingContext(
                T0.minusHours(1),
                DomainFixtures.defaultEventMode(),
                Map.of(20, snapshot)
        );

        final RejectedCandidate rejected = filter.rejectOrNull(task, candidate, context);

        assertThat(rejected).isNotNull();
        assertThat(rejected.reason()).isEqualTo(RejectionReason.TIME_CONFLICT);
    }

    @Test
    void rejects_when_required_skill_category_missing() {
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        task.addRequiredSkill(DomainFixtures.skill(5, "Medic", "Medical"));
        final User candidate = DomainFixtures.user(20);
        final MatchingContext context = DomainFixtures.matchingContext(
                T0.minusHours(1),
                DomainFixtures.defaultEventMode(),
                Map.of(20, CandidateSnapshot.empty())
        );

        final RejectedCandidate rejected = filter.rejectOrNull(task, candidate, context);

        assertThat(rejected).isNotNull();
        assertThat(rejected.reason()).isEqualTo(RejectionReason.MISSING_REQUIRED_SKILLS);
    }

    @Test
    void rejects_when_completed_work_today_plus_new_task_exceeds_daily_cap() {
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final User candidate = DomainFixtures.user(20);
        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                Duration.ofHours(7),
                Map.of(),
                Map.of(),
                Map.of());
        final MatchingContext context = DomainFixtures.matchingContext(
                T0.minusHours(1),
                DomainFixtures.defaultEventMode(),
                Map.of(20, snapshot)
        );

        final RejectedCandidate rejected = filter.rejectOrNull(task, candidate, context);

        assertThat(rejected).isNotNull();
        assertThat(rejected.reason()).isEqualTo(RejectionReason.DAILY_LOAD_EXCEEDED);
    }

    @Test
    void rejects_when_daily_load_would_be_exceeded() {
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final User candidate = DomainFixtures.user(20);
        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                Duration.ofHours(7).plusMinutes(45),
                Map.of(),
                Map.of(),
                Map.of());
        final MatchingContext context = DomainFixtures.matchingContext(
                T0.minusHours(1),
                DomainFixtures.defaultEventMode(),
                Map.of(20, snapshot)
        );

        final RejectedCandidate rejected = filter.rejectOrNull(task, candidate, context);

        assertThat(rejected).isNotNull();
        assertThat(rejected.reason()).isEqualTo(RejectionReason.DAILY_LOAD_EXCEEDED);
    }

    @Test
    void accepts_when_constraints_satisfied() {
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final User candidate = DomainFixtures.user(20);
        final MatchingContext context = DomainFixtures.matchingContext(
                T0.minusHours(1),
                DomainFixtures.defaultEventMode(),
                Map.of(20, CandidateSnapshot.empty())
        );

        final RejectedCandidate rejected = filter.rejectOrNull(task, candidate, context);

        assertThat(rejected).isNull();
    }

    @Test
    void accepts_when_worked_today_null_counts_as_zero_for_daily_cap() {
        final User creator = DomainFixtures.user(1);
        final var event = DomainFixtures.event(1, creator);
        final Task task = DomainFixtures.openTask(10, event, creator, T0, T1);
        final User candidate = DomainFixtures.user(20);
        final CandidateSnapshot snapshot = new CandidateSnapshot(
                null,
                List.of(),
                null,
                null,
                Duration.ZERO,
                null,
                Map.of(),
                Map.of(),
                Map.of());
        final MatchingContext context = DomainFixtures.matchingContext(
                T0.minusHours(1),
                DomainFixtures.defaultEventMode(),
                Map.of(20, snapshot)
        );

        assertThat(filter.rejectOrNull(task, candidate, context)).isNull();
    }
}
