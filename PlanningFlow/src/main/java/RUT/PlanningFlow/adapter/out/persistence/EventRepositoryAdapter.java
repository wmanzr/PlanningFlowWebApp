package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.EventEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.EventRepository;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class EventRepositoryAdapter implements EventRepositoryPort {

    private final EventRepository repository;

    public EventRepositoryAdapter(final EventRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Event> findById(final Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findByIdEagerCreatorAndCoordinators(id).map(EntityToDomainMapper::toDomain);
    }

    @Override
    public List<Event> findAll() {
        final List<EventEntity> entities = repository.findAll();
        final List<Event> items = new ArrayList<>(entities.size());
        for (final EventEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public PageResult<Event> findEvents(final PageQuery pageQuery) {
        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page = repository.findAllByOrderByStartDateAscIdAsc(pageable);

        final List<Event> items = new ArrayList<>(page.getContent().size());
        for (final EventEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }

        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Event> findByTitleContainingIgnoreCase(final String searchTerm, final PageQuery pageQuery) {
        final String normalized = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
        if (normalized.isBlank()) {
            return findEvents(pageQuery);
        }

        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page = repository.findByTitleContainingIgnoreCaseOrderByStartDateAscIdAsc(normalized, pageable);

        final List<Event> items = new ArrayList<>(page.getContent().size());
        for (final EventEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }

        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Event> findAccessibleByUser(final Integer userId, final PageQuery pageQuery) {
        if (userId == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page = repository.findAccessibleByUser(userId, pageable);
        final List<Event> items = new ArrayList<>(page.getContent().size());
        for (final EventEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Event> findAccessibleByUserAndStartDateBetween(
            final Integer userId,
            final LocalDateTime start,
            final LocalDateTime end,
            final PageQuery pageQuery
    ) {
        if (userId == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        DomainAssert.notNull(start, "Дата начала обязательна", "EVENT_START_DATE_REQUIRED");
        DomainAssert.notNull(end, "Дата окончания обязательна", "EVENT_END_DATE_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page = repository.findAccessibleByUserAndStartDateBetween(userId, start, end, pageable);
        final List<Event> items = new ArrayList<>(page.getContent().size());
        for (final EventEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Event> findByCreator(final Integer creatorId, final PageQuery pageQuery) {
        if (creatorId == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page = repository.findByCreator_IdOrderByStartDateAscIdAsc(creatorId, pageable);
        final List<Event> items = new ArrayList<>(page.getContent().size());
        for (final EventEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Event> findByCreatorAndStartDateBetween(
            final Integer creatorId,
            final LocalDateTime start,
            final LocalDateTime end,
            final PageQuery pageQuery
    ) {
        if (creatorId == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        DomainAssert.notNull(start, "Дата начала обязательна", "EVENT_START_DATE_REQUIRED");
        DomainAssert.notNull(end, "Дата окончания обязательна", "EVENT_END_DATE_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page =
                repository.findByCreator_IdAndStartDateBetweenOrderByStartDateAscIdAsc(creatorId, start, end, pageable);
        final List<Event> items = new ArrayList<>(page.getContent().size());
        for (final EventEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Event> findByCoordinator(final Integer coordinatorId, final PageQuery pageQuery) {
        if (coordinatorId == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page = repository.findByCoordinator(coordinatorId, pageable);
        final List<Event> items = new ArrayList<>(page.getContent().size());
        for (final EventEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Event> findByCoordinatorAndStartDateBetween(
            final Integer coordinatorId,
            final LocalDateTime start,
            final LocalDateTime end,
            final PageQuery pageQuery
    ) {
        if (coordinatorId == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        DomainAssert.notNull(start, "Дата начала обязательна", "EVENT_START_DATE_REQUIRED");
        DomainAssert.notNull(end, "Дата окончания обязательна", "EVENT_END_DATE_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page = repository.findByCoordinatorAndStartDateBetween(coordinatorId, start, end, pageable);
        final List<Event> items = new ArrayList<>(page.getContent().size());
        for (final EventEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Event> findByStartDateBetween(
            final LocalDateTime start,
            final LocalDateTime end,
            final PageQuery pageQuery
    ) {
        DomainAssert.notNull(start, "Дата начала обязательна", "EVENT_START_DATE_REQUIRED");
        DomainAssert.notNull(end, "Дата окончания обязательна", "EVENT_END_DATE_REQUIRED");

        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page = repository.findByStartDateBetweenOrderByStartDateAscIdAsc(start, end, pageable);

        final List<Event> items = new ArrayList<>(page.getContent().size());
        for (final EventEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }

        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Event> findByTitleContainingIgnoreCaseAndStartDateBetween(
            final String title,
            final LocalDateTime start,
            final LocalDateTime end,
            final PageQuery pageQuery
    ) {
        DomainAssert.notNull(title, "Строка поиска обязательна", "EVENT_SEARCH_REQUIRED");
        DomainAssert.notNull(start, "Дата начала обязательна", "EVENT_START_DATE_REQUIRED");
        DomainAssert.notNull(end, "Дата окончания обязательна", "EVENT_END_DATE_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page =
                repository.findByTitleContainingIgnoreCaseAndStartDateBetweenOrderByStartDateAscIdAsc(title, start, end, pageable);
        return toEventPageResult(page);
    }

    @Override
    public PageResult<Event> findByCreatorAndTitleContainingIgnoreCase(
            final Integer creatorId,
            final String title,
            final PageQuery pageQuery
    ) {
        if (creatorId == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        DomainAssert.notNull(title, "Строка поиска обязательна", "EVENT_SEARCH_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page =
                repository.findByCreator_IdAndTitleContainingIgnoreCaseOrderByStartDateAscIdAsc(creatorId, title, pageable);
        return toEventPageResult(page);
    }

    @Override
    public PageResult<Event> findByCreatorAndTitleContainingIgnoreCaseAndStartDateBetween(
            final Integer creatorId,
            final String title,
            final LocalDateTime start,
            final LocalDateTime end,
            final PageQuery pageQuery
    ) {
        if (creatorId == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        DomainAssert.notNull(title, "Строка поиска обязательна", "EVENT_SEARCH_REQUIRED");
        DomainAssert.notNull(start, "Дата начала обязательна", "EVENT_START_DATE_REQUIRED");
        DomainAssert.notNull(end, "Дата окончания обязательна", "EVENT_END_DATE_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page = repository.findByCreator_IdAndTitleContainingIgnoreCaseAndStartDateBetweenOrderByStartDateAscIdAsc(
                creatorId,
                title,
                start,
                end,
                pageable
        );
        return toEventPageResult(page);
    }

    @Override
    public PageResult<Event> findByCoordinatorAndTitleContainingIgnoreCase(
            final Integer coordinatorId,
            final String title,
            final PageQuery pageQuery
    ) {
        if (coordinatorId == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        DomainAssert.notNull(title, "Строка поиска обязательна", "EVENT_SEARCH_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page = repository.findByCoordinatorAndTitleContaining(coordinatorId, title, pageable);
        return toEventPageResult(page);
    }

    @Override
    public PageResult<Event> findByCoordinatorAndTitleContainingIgnoreCaseAndStartDateBetween(
            final Integer coordinatorId,
            final String title,
            final LocalDateTime start,
            final LocalDateTime end,
            final PageQuery pageQuery
    ) {
        if (coordinatorId == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        DomainAssert.notNull(title, "Строка поиска обязательна", "EVENT_SEARCH_REQUIRED");
        DomainAssert.notNull(start, "Дата начала обязательна", "EVENT_START_DATE_REQUIRED");
        DomainAssert.notNull(end, "Дата окончания обязательна", "EVENT_END_DATE_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page =
                repository.findByCoordinatorAndTitleContainingAndStartDateBetween(coordinatorId, title, start, end, pageable);
        return toEventPageResult(page);
    }

    @Override
    public PageResult<Event> findAccessibleByUserAndTitleContainingIgnoreCase(
            final Integer userId,
            final String title,
            final PageQuery pageQuery
    ) {
        if (userId == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        DomainAssert.notNull(title, "Строка поиска обязательна", "EVENT_SEARCH_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page = repository.findAccessibleByUserAndTitleContaining(userId, title, pageable);
        return toEventPageResult(page);
    }

    @Override
    public PageResult<Event> findAccessibleByUserAndTitleContainingIgnoreCaseAndStartDateBetween(
            final Integer userId,
            final String title,
            final LocalDateTime start,
            final LocalDateTime end,
            final PageQuery pageQuery
    ) {
        if (userId == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        DomainAssert.notNull(title, "Строка поиска обязательна", "EVENT_SEARCH_REQUIRED");
        DomainAssert.notNull(start, "Дата начала обязательна", "EVENT_START_DATE_REQUIRED");
        DomainAssert.notNull(end, "Дата окончания обязательна", "EVENT_END_DATE_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        final Pageable pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<EventEntity> page =
                repository.findAccessibleByUserAndTitleContainingAndStartDateBetween(userId, title, start, end, pageable);
        return toEventPageResult(page);
    }

    private PageResult<Event> toEventPageResult(final Page<EventEntity> page) {
        final List<Event> items = new ArrayList<>(page.getContent().size());
        for (final EventEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public long countByCreatorIdAndStatus(final Integer creatorId, final EventStatus status) {
        if (creatorId == null || status == null) {
            return 0L;
        }
        return repository.countByCreatorIdAndStatus(creatorId, status);
    }

    @Override
    public long countByStatus(final EventStatus status) {
        if (status == null) {
            return 0L;
        }
        return repository.countByStatus(status);
    }

    @Override
    public long countAllEvents() {
        return repository.countAllEvents();
    }

    @Override
    public List<Event> findEventsWhereCoordinatorUnderOrganizer(
            final Integer organizerId,
            final Integer coordinatorId,
            final List<EventStatus> statuses
    ) {
        if (organizerId == null || coordinatorId == null || statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        final List<EventEntity> entities = repository.findEventsWhereCoordinatorUnderOrganizer(organizerId, coordinatorId, statuses);
        final List<Event> items = new ArrayList<>(entities.size());
        for (final EventEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public List<Event> findEventsSharedBetweenCoordinatorAndParticipant(
            final Integer coordinatorId,
            final Integer participantId
    ) {
        if (coordinatorId == null || participantId == null) {
            return List.of();
        }
        final List<EventEntity> entities =
                repository.findEventsSharedBetweenCoordinatorAndParticipant(coordinatorId, participantId);
        final List<Event> items = new ArrayList<>(entities.size());
        for (final EventEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public long countEventsWhereCoordinator(final Integer coordinatorId) {
        if (coordinatorId == null) {
            return 0L;
        }
        return repository.countWhereCoordinator(coordinatorId);
    }

    @Override
    public long countEventsWhereCoordinatorUnderOrganizer(final Integer organizerId, final Integer coordinatorId) {
        if (organizerId == null || coordinatorId == null) {
            return 0L;
        }
        return repository.countWhereCoordinatorUnderOrganizer(organizerId, coordinatorId);
    }

    @Override
    public long countCompletedEventsWhereCreatorOrCoordinator(final Integer userId) {
        if (userId == null) {
            return 0L;
        }
        return repository.countCompletedEventsWhereCreatorOrCoordinator(userId);
    }

    @Override
    public long countEventsCreatedByUser(final Integer creatorId) {
        if (creatorId == null) {
            return 0L;
        }
        return repository.countEventsCreatedByUser(creatorId);
    }

    @Override
    public Optional<Integer> create(final Event event) {
        if (event == null) {
            return Optional.empty();
        }
        final EventEntity entity = DomainToEntityMapper.toEntity(event);
        final EventEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public Optional<Integer> update(final Event event) {
        if (event == null || event.getId() == null) {
            return Optional.empty();
        }

        final EventEntity entity = repository.findByIdEagerCreatorAndCoordinators(event.getId()).orElse(null);
        if (entity == null) {
            return Optional.empty();
        }

        DomainToEntityMapper.applyToEntity(event, entity);
        final EventEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }
}