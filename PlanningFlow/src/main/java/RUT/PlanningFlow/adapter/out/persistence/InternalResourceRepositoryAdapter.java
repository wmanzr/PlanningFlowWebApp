package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.InternalResourceEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.InternalResourceRepository;
import RUT.PlanningFlow.adapter.out.persistence.repository.ResourceBookingRepository;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.out.repository.InternalResourceRepositoryPort;
import RUT.PlanningFlow.domain.model.InternalResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InternalResourceRepositoryAdapter implements InternalResourceRepositoryPort {

    private final InternalResourceRepository repository;
    private final ResourceBookingRepository resourceBookingRepository;

    public InternalResourceRepositoryAdapter(
            final InternalResourceRepository repository,
            final ResourceBookingRepository resourceBookingRepository
    ) {
        this.repository = repository;
        this.resourceBookingRepository = resourceBookingRepository;
    }

    @Override
    public Optional<InternalResource> findById(final Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id).map(e -> (InternalResource) EntityToDomainMapper.toDomain(e));
    }

    @Override
    public List<InternalResource> findAll() {
        final List<InternalResourceEntity> entities = repository.findAll();
        final List<InternalResource> items = new ArrayList<>(entities.size());
        for (final InternalResourceEntity e : entities) {
            items.add((InternalResource) EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public PageResult<InternalResource> findInternalResources(final PageQuery pageQuery) {
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<InternalResourceEntity> page = repository.findAllByOrderByNameAscIdAsc(pageable);

        final List<InternalResource> items = new ArrayList<>(page.getContent().size());
        for (final InternalResourceEntity e : page.getContent()) {
            items.add((InternalResource) EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<InternalResource> findByNameContainingIgnoreCase(final String searchTerm, final PageQuery pageQuery) {
        final String normalized = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
        if (normalized.isBlank()) {
            return findInternalResources(pageQuery);
        }

        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<InternalResourceEntity> page = repository.findByNameContainingIgnoreCaseOrderByNameAscIdAsc(normalized, pageable);

        final List<InternalResource> items = new ArrayList<>(page.getContent().size());
        for (final InternalResourceEntity e : page.getContent()) {
            items.add((InternalResource) EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public Optional<Integer> create(final InternalResource resource) {
        if (resource == null) {
            return Optional.empty();
        }
        final InternalResourceEntity entity = DomainToEntityMapper.toEntity(resource);
        final InternalResourceEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public Optional<Integer> update(final InternalResource resource) {
        if (resource == null || resource.getId() == null) {
            return Optional.empty();
        }

        final Optional<InternalResourceEntity> existing = repository.findById(resource.getId());
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        final InternalResourceEntity entity = existing.get();
        DomainToEntityMapper.applyToEntity(resource, entity);

        final InternalResourceEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public boolean deleteById(final Integer id) {
        if (id == null) {
            return false;
        }
        final Optional<InternalResourceEntity> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return false;
        }
        resourceBookingRepository.deleteByResource_Id(id);
        repository.delete(existing.get());
        return true;
    }
}

