package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.ExternalResourceEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.InternalResourceEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.ResourceBookingEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.TaskEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.ResourceBookingRepository;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.out.repository.ResourceBookingRepositoryPort;
import RUT.PlanningFlow.domain.enums.BookingStatus;
import RUT.PlanningFlow.domain.model.ExternalResource;
import RUT.PlanningFlow.domain.model.InternalResource;
import RUT.PlanningFlow.domain.model.Resource;
import RUT.PlanningFlow.domain.model.ResourceBooking;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ResourceBookingRepositoryAdapter implements ResourceBookingRepositoryPort {

    private final ResourceBookingRepository repository;
    private final EntityManager entityManager;

    public ResourceBookingRepositoryAdapter(
            final ResourceBookingRepository repository,
            final EntityManager entityManager
    ) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<ResourceBooking> findById(final Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id).map(EntityToDomainMapper::toDomain);
    }

    @Override
    public List<ResourceBooking> findAll() {
        final List<ResourceBookingEntity> entities = repository.findAll();
        final List<ResourceBooking> items = new ArrayList<>(entities.size());
        for (final ResourceBookingEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public PageResult<ResourceBooking> findResourceBookings(final PageQuery pageQuery) {
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<ResourceBookingEntity> page = repository.findAllByOrderByReservedFromAscIdAsc(pageable);

        final List<ResourceBooking> items = new ArrayList<>(page.getContent().size());
        for (final ResourceBookingEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }

        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<ResourceBooking> findByResourceNameContainingIgnoreCase(
            final String searchTerm,
            final PageQuery pageQuery
    ) {
        final String normalized = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
        if (normalized.isBlank()) {
            return findResourceBookings(pageQuery);
        }

        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<ResourceBookingEntity> page = repository.findByResource_NameContainingIgnoreCaseOrderByReservedFromAscIdAsc(
                normalized,
                pageable
        );

        final List<ResourceBooking> items = new ArrayList<>(page.getContent().size());
        for (final ResourceBookingEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }

        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public Optional<Integer> create(final ResourceBooking booking) {
        if (booking == null) {
            return Optional.empty();
        }
        final ResourceBookingEntity entity = DomainToEntityMapper.toEntity(booking);
        attachPersistentReferences(entity, booking);
        final ResourceBookingEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public Optional<Integer> update(final ResourceBooking booking) {
        if (booking == null || booking.getId() == null) {
            return Optional.empty();
        }

        final Optional<ResourceBookingEntity> existing = repository.findById(booking.getId());
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        final ResourceBookingEntity entity = existing.get();
        DomainToEntityMapper.applyToEntity(booking, entity);
        attachPersistentReferences(entity, booking);

        final ResourceBookingEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    
    private void attachPersistentReferences(final ResourceBookingEntity entity, final ResourceBooking booking) {
        final Integer taskId = booking.getTask() == null ? null : booking.getTask().getId();
        if (taskId != null) {
            entity.setTask(entityManager.getReference(TaskEntity.class, taskId));
        }
        final Resource r = booking.getResource();
        final Integer resourceId = r == null ? null : r.getId();
        if (resourceId != null) {
            if (r instanceof InternalResource) {
                entity.setResource(entityManager.getReference(InternalResourceEntity.class, resourceId));
            } else if (r instanceof ExternalResource) {
                entity.setResource(entityManager.getReference(ExternalResourceEntity.class, resourceId));
            }
        }
    }

    @Override
    public PageResult<ResourceBooking> findByTaskId(final Integer taskId, final PageQuery pageQuery) {
        if (taskId == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<ResourceBookingEntity> page = repository.findByTask_IdOrderByReservedFromAscIdAsc(taskId, pageable);

        final List<ResourceBooking> items = new ArrayList<>(page.getContent().size());
        for (final ResourceBookingEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }

        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public List<ResourceBooking> findActiveForTask(final Integer taskId) {
        if (taskId == null) {
            return List.of();
        }
        final List<ResourceBookingEntity> entities = repository.findByTask_IdAndStatusIn(
                taskId,
                List.of(BookingStatus.REQUESTED, BookingStatus.CONFIRMED)
        );
        final List<ResourceBooking> items = new ArrayList<>(entities.size());
        for (final ResourceBookingEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public long countBookingsForEventsWhereCreatorOrCoordinator(final Integer userId) {
        if (userId == null) {
            return 0L;
        }
        return repository.countBookingsForEventsWhereCreatorOrCoordinator(userId);
    }

    @Override
    public long countBookingsForEventsWhereCreator(final Integer organizerId) {
        if (organizerId == null) {
            return 0L;
        }
        return repository.countBookingsForEventsWhereCreator(organizerId);
    }
}
