package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.ResourceEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.ResourceRepository;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.out.repository.ResourceRepositoryPort;
import RUT.PlanningFlow.domain.model.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ResourceRepositoryAdapter implements ResourceRepositoryPort {

    private final ResourceRepository repository;

    public ResourceRepositoryAdapter(final ResourceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Resource> findById(final Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id).map(EntityToDomainMapper::toDomain);
    }

    @Override
    public List<Resource> findAll() {
        final List<ResourceEntity> entities = repository.findAll();
        final List<Resource> items = new ArrayList<>(entities.size());
        for (final ResourceEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public PageResult<Resource> findResources(final PageQuery pageQuery) {
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<ResourceEntity> page = repository.findAllByOrderByNameAscIdAsc(pageable);

        final List<Resource> items = new ArrayList<>(page.getContent().size());
        for (final ResourceEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Resource> findByNameContainingIgnoreCase(final String searchTerm, final PageQuery pageQuery) {
        final String normalized = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
        if (normalized.isBlank()) {
            return findResources(pageQuery);
        }

        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<ResourceEntity> page = repository.findByNameContainingIgnoreCaseOrderByNameAscIdAsc(normalized, pageable);

        final List<Resource> items = new ArrayList<>(page.getContent().size());
        for (final ResourceEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public Optional<Integer> create(final Resource resource) {
        if (resource == null) {
            return Optional.empty();
        }
        final ResourceEntity entity = DomainToEntityMapper.toEntity(resource);
        final ResourceEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public Optional<Integer> update(final Resource resource) {
        if (resource == null || resource.getId() == null) {
            return Optional.empty();
        }

        final Optional<ResourceEntity> existing = repository.findById(resource.getId());
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        final ResourceEntity entity = existing.get();
        if (resource instanceof RUT.PlanningFlow.domain.model.InternalResource ir && entity instanceof RUT.PlanningFlow.adapter.out.persistence.entity.InternalResourceEntity ie) {
            DomainToEntityMapper.applyToEntity(ir, ie);
        } else if (resource instanceof RUT.PlanningFlow.domain.model.ExternalResource er && entity instanceof RUT.PlanningFlow.adapter.out.persistence.entity.ExternalResourceEntity ee) {
            DomainToEntityMapper.applyToEntity(er, ee);
        } else {
            return Optional.empty();
        }

        final ResourceEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }
}

