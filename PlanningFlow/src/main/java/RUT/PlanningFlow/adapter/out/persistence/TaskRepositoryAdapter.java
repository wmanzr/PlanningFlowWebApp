package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.TaskEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.TaskRepository;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepositoryAdapter implements TaskRepositoryPort {

    private final TaskRepository repository;

    public TaskRepositoryAdapter(final TaskRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Task> findById(final Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id).map(EntityToDomainMapper::toDomain);
    }

    @Override
    public List<Task> findAll() {
        final List<TaskEntity> entities = repository.findAll();
        final List<Task> items = new ArrayList<>(entities.size());
        for (final TaskEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public PageResult<Task> findTasks(final PageQuery pageQuery) {
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<TaskEntity> page = repository.findAllByOrderByStartTimeAscIdAsc(pageable);

        final List<Task> items = new ArrayList<>(page.getContent().size());
        for (final TaskEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }

        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Task> findByTitleContainingIgnoreCase(final String searchTerm, final PageQuery pageQuery) {
        final String normalized = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
        if (normalized.isBlank()) {
            return findTasks(pageQuery);
        }

        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<TaskEntity> page = repository.findByTitleContainingIgnoreCaseOrderByStartTimeAscIdAsc(
                normalized,
                pageable
        );

        final List<Task> items = new ArrayList<>(page.getContent().size());
        for (final TaskEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }

        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public List<Task> findCommittedTasksForUserOnDate(final Integer userId, final LocalDate date) {
        final List<TaskEntity> entities = repository.findCommittedTasksForUserOnDate(
                userId,
                List.of(AssignStatus.PENDING, AssignStatus.ACCEPTED),
                List.of(TaskStatus.DONE, TaskStatus.CANCELLED),
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
        );

        final List<Task> items = new ArrayList<>(entities.size());
        for (final TaskEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public PageResult<Task> findTasksForUser(
            final Integer userId,
            final List<AssignStatus> assignmentStatuses,
            final String titleOrNull,
            final PageQuery pageQuery
    ) {
        DomainAssert.notNull(userId, "ID пользователя обязателен", "USER_ID_REQUIRED");
        DomainAssert.notNull(assignmentStatuses, "Статусы назначения обязательны", "ASSIGNMENT_STATUSES_REQUIRED");
        DomainAssert.isTrue(!assignmentStatuses.isEmpty(), "Список статусов не может быть пустым", "ASSIGNMENT_STATUSES_EMPTY");

        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final String title = titleOrNull == null || titleOrNull.isBlank() ? null : titleOrNull.trim();
        final Page<TaskEntity> page =
                title == null
                        ? repository.findTasksForUser(userId, assignmentStatuses, pageable)
                        : repository.findTasksForUserWithTitle(userId, assignmentStatuses, title, pageable);

        final List<Task> items = new ArrayList<>(page.getContent().size());
        for (final TaskEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Task> findTasksForUserBetween(
            final Integer userId,
            final List<AssignStatus> assignmentStatuses,
            final LocalDateTime start,
            final LocalDateTime end,
            final String titleOrNull,
            final PageQuery pageQuery
    ) {
        DomainAssert.notNull(userId, "ID пользователя обязателен", "USER_ID_REQUIRED");
        DomainAssert.notNull(assignmentStatuses, "Статусы назначения обязательны", "ASSIGNMENT_STATUSES_REQUIRED");
        DomainAssert.isTrue(!assignmentStatuses.isEmpty(), "Список статусов не может быть пустым", "ASSIGNMENT_STATUSES_EMPTY");
        DomainAssert.notNull(start, "Дата начала обязательна", "TASK_START_DATE_REQUIRED");
        DomainAssert.notNull(end, "Дата окончания обязательна", "TASK_END_DATE_REQUIRED");

        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final String title = titleOrNull == null || titleOrNull.isBlank() ? null : titleOrNull.trim();
        final Page<TaskEntity> page =
                title == null
                        ? repository.findTasksForUserBetween(userId, assignmentStatuses, start, end, pageable)
                        : repository.findTasksForUserBetweenWithTitle(
                                userId, assignmentStatuses, start, end, title, pageable);

        final List<Task> items = new ArrayList<>(page.getContent().size());
        for (final TaskEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public boolean existsByEventIdAndTitleIgnoreCase(final Integer eventId, final String title) {
        if (eventId == null || title == null || title.isBlank()) {
            return false;
        }
        return repository.existsByEventIdAndTitleIgnoreCase(eventId, title.trim());
    }

    @Override
    public boolean existsByEventIdAndTitleIgnoreCaseAndIdNot(
            final Integer eventId,
            final String title,
            final Integer excludeTaskId
    ) {
        if (eventId == null || title == null || title.isBlank() || excludeTaskId == null) {
            return false;
        }
        return repository.existsByEventIdAndTitleIgnoreCaseAndIdNot(eventId, title.trim(), excludeTaskId);
    }

    @Override
    public List<Task> findTasksForEvent(final Integer eventId) {
        if (eventId == null) {
            return List.of();
        }
        final List<TaskEntity> entities = repository.findByEventIdOrderByStartTimeAscIdAsc(eventId);
        final List<Task> items = new ArrayList<>(entities.size());
        for (final TaskEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public PageResult<Task> findTasksForEvent(final Integer eventId, final PageQuery pageQuery) {
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<TaskEntity> page = repository.findByEventIdOrderByStartTimeAscIdAsc(eventId, pageable);

        final List<Task> items = new ArrayList<>(page.getContent().size());
        for (final TaskEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Task> findTasksForEventBetween(
            final Integer eventId,
            final LocalDateTime start,
            final LocalDateTime end,
            final PageQuery pageQuery
    ) {
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        DomainAssert.notNull(start, "Дата начала обязательна", "TASK_START_DATE_REQUIRED");
        DomainAssert.notNull(end, "Дата окончания обязательна", "TASK_END_DATE_REQUIRED");

        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<TaskEntity> page = repository.findByEventIdAndStartTimeBetweenOrderByStartTimeAscIdAsc(eventId, start, end, pageable);

        final List<Task> items = new ArrayList<>(page.getContent().size());
        for (final TaskEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public Optional<Integer> create(final Task task) {
        if (task == null) {
            return Optional.empty();
        }
        final TaskEntity entity = DomainToEntityMapper.toEntity(task);
        final TaskEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public Optional<Integer> update(final Task task) {
        if (task == null || task.getId() == null) {
            return Optional.empty();
        }

        final TaskEntity entity = repository.findById(task.getId()).orElse(null);
        if (entity == null) {
            return Optional.empty();
        }

        DomainToEntityMapper.applyToEntity(task, entity);
        final TaskEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public long countTasksAuthoredByUser(final Integer userId) {
        if (userId == null) {
            return 0L;
        }
        return repository.countTasksAuthoredByUser(userId);
    }

    @Override
    public long countByEventIdAndStatus(final Integer eventId, final TaskStatus status) {
        if (eventId == null || status == null) {
            return 0L;
        }
        return repository.countByEventIdAndStatus(eventId, status);
    }

    @Override
    public long countByStatus(final TaskStatus status) {
        if (status == null) {
            return 0L;
        }
        return repository.countByStatus(status);
    }

    @Override
    public long countTasksForEvent(final Integer eventId) {
        if (eventId == null) {
            return 0L;
        }
        return repository.countTasksForEvent(eventId);
    }

    @Override
    public double sumCompletedWorkedHoursForUser(final Integer userId) {
        if (userId == null) {
            return 0.0;
        }
        final Double raw = repository.sumCompletedWorkedHours(userId);
        if (raw == null || raw.isNaN() || raw.isInfinite()) {
            return 0.0;
        }
        return raw;
    }
}