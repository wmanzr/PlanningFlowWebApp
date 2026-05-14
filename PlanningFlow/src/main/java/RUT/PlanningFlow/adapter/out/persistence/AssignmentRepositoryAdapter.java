package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.AssignmentEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.AssignmentRepository;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.model.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AssignmentRepositoryAdapter implements AssignmentRepositoryPort {

    private final AssignmentRepository repository;

    public AssignmentRepositoryAdapter(final AssignmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Assignment> findById(final Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id).map(EntityToDomainMapper::toDomain);
    }

    @Override
    public Optional<Assignment> findActiveForTaskAndUser(final Integer taskId, final Integer userId) {
        if (taskId == null || userId == null) {
            return Optional.empty();
        }
        return repository.findFirstByTask_IdAndUser_IdAndStatusInOrderByAssignedAtDescIdDesc(
                taskId,
                userId,
                List.of(AssignStatus.PENDING, AssignStatus.ACCEPTED)
        ).map(EntityToDomainMapper::toDomain);
    }

    @Override
    public Optional<Assignment> findLatestForTaskAndUser(final Integer taskId, final Integer userId) {
        if (taskId == null || userId == null) {
            return Optional.empty();
        }
        return repository.findFirstByTask_IdAndUser_IdOrderByAssignedAtDescIdDesc(taskId, userId)
                .map(EntityToDomainMapper::toDomain);
    }

    @Override
    public List<Assignment> findByTaskId(final Integer taskId) {
        if (taskId == null) {
            return List.of();
        }
        final List<AssignmentEntity> entities = repository.findByTask_IdOrderByAssignedAtAscIdAsc(taskId);
        final List<Assignment> items = new ArrayList<>(entities.size());
        for (final AssignmentEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public List<Assignment> findAll() {
        final List<AssignmentEntity> entities = repository.findAll();
        final List<Assignment> items = new ArrayList<>(entities.size());
        for (final AssignmentEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public PageResult<Assignment> findAssignments(final PageQuery pageQuery) {
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<AssignmentEntity> page = repository.findAllByOrderByAssignedAtDescIdDesc(pageable);

        final List<Assignment> items = new ArrayList<>(page.getContent().size());
        for (final AssignmentEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }

        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Assignment> findByTaskTitleContainingIgnoreCase(final String searchTerm, final PageQuery pageQuery) {
        final String normalized = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
        if (normalized.isBlank()) {
            return findAssignments(pageQuery);
        }

        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<AssignmentEntity> page = repository.findByTask_TitleContainingIgnoreCaseOrderByAssignedAtDescIdDesc(
                normalized,
                pageable
        );

        final List<Assignment> items = new ArrayList<>(page.getContent().size());
        for (final AssignmentEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }

        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public Optional<Integer> create(final Assignment assignment) {
        if (assignment == null) {
            return Optional.empty();
        }
        final AssignmentEntity entity = DomainToEntityMapper.toEntity(assignment);
        final AssignmentEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public List<Assignment> findAllWithTaskAndEventForUser(final Integer userId) {
        if (userId == null) {
            return List.of();
        }
        final List<AssignmentEntity> entities = repository.findAllForUserWithTaskAndEvent(userId);
        final List<Assignment> items = new ArrayList<>(entities.size());
        for (final AssignmentEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public boolean existsAssignmentForUserOnEvent(final Integer userId, final Integer eventId) {
        if (userId == null || eventId == null) {
            return false;
        }
        return repository.existsByUser_IdAndTask_Event_Id(userId, eventId);
    }

    @Override
    public boolean existsAssignmentForUserOnTask(final Integer userId, final Integer taskId) {
        if (userId == null || taskId == null) {
            return false;
        }
        return repository.existsByUser_IdAndTask_IdAndStatusIn(
                userId,
                taskId,
                List.of(AssignStatus.PENDING, AssignStatus.ACCEPTED)
        );
    }

    @Override
    public long countCompletedTasksForUser(final Integer userId) {
        if (userId == null) {
            return 0L;
        }
        return repository.countCompletedTasksForUser(userId);
    }

    @Override
    public long countDistinctEventsParticipatedForUser(final Integer userId) {
        if (userId == null) {
            return 0L;
        }
        return repository.countDistinctEventsParticipatedForUser(userId);
    }

    @Override
    public long countDistinctEventsForParticipantUnderViewer(
            final Integer participantId,
            final Integer viewerId,
            final List<EventStatus> statuses
    ) {
        if (participantId == null || viewerId == null || statuses == null || statuses.isEmpty()) {
            return 0L;
        }
        return repository.countDistinctEventsForParticipantUnderViewer(participantId, viewerId, statuses);
    }

    @Override
    public long countDistinctEventsWithAssignmentsForParticipant(final Integer participantId) {
        if (participantId == null) {
            return 0L;
        }
        return repository.countDistinctEventsWithAssignmentsForParticipant(participantId);
    }

    @Override
    public List<Assignment> findForParticipantUnderOrganizer(
            final Integer participantId,
            final Integer organizerId,
            final List<EventStatus> statuses
    ) {
        if (participantId == null || organizerId == null || statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        final List<AssignmentEntity> entities = repository.findForParticipantUnderOrganizer(participantId, organizerId, statuses);
        final List<Assignment> items = new ArrayList<>(entities.size());
        for (final AssignmentEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public List<Assignment> findForParticipantUnderCoordinator(
            final Integer participantId,
            final Integer coordinatorId,
            final List<EventStatus> statuses
    ) {
        if (participantId == null || coordinatorId == null || statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        final List<AssignmentEntity> entities =
                repository.findForParticipantUnderCoordinator(participantId, coordinatorId, statuses);
        final List<Assignment> items = new ArrayList<>(entities.size());
        for (final AssignmentEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public long countByStatus(final AssignStatus status) {
        if (status == null) {
            return 0L;
        }
        return repository.countByStatus(status);
    }

    @Override
    public Optional<Integer> update(final Assignment assignment) {
        if (assignment == null || assignment.getId() == null) {
            return Optional.empty();
        }

        final Optional<AssignmentEntity> existing = repository.findById(assignment.getId());
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        final AssignmentEntity entity = existing.get();
        DomainToEntityMapper.applyToEntity(assignment, entity);

        final AssignmentEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }
}

