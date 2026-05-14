package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.ExternalResourceEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.ExternalResourceRepository;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.out.repository.ExternalResourceRepositoryPort;
import RUT.PlanningFlow.domain.model.ExternalResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ExternalResourceRepositoryAdapter implements ExternalResourceRepositoryPort {

    private final ExternalResourceRepository repository;

    public ExternalResourceRepositoryAdapter(final ExternalResourceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ExternalResource> findById(final Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id).map(e -> (ExternalResource) EntityToDomainMapper.toDomain(e));
    }

    @Override
    public List<ExternalResource> findAll() {
        final List<ExternalResourceEntity> entities = repository.findAll();
        final List<ExternalResource> items = new ArrayList<>(entities.size());
        for (final ExternalResourceEntity e : entities) {
            items.add((ExternalResource) EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public PageResult<ExternalResource> findExternalResources(final PageQuery pageQuery) {
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<ExternalResourceEntity> page = repository.findAllByOrderByNameAscIdAsc(pageable);

        final List<ExternalResource> items = new ArrayList<>(page.getContent().size());
        for (final ExternalResourceEntity e : page.getContent()) {
            items.add((ExternalResource) EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<ExternalResource> findByNameContainingIgnoreCase(final String searchTerm, final PageQuery pageQuery) {
        final String normalized = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
        if (normalized.isBlank()) {
            return findExternalResources(pageQuery);
        }

        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<ExternalResourceEntity> page = repository.findByNameContainingIgnoreCaseOrderByNameAscIdAsc(normalized, pageable);

        final List<ExternalResource> items = new ArrayList<>(page.getContent().size());
        for (final ExternalResourceEntity e : page.getContent()) {
            items.add((ExternalResource) EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public Optional<Integer> create(final ExternalResource resource) {
        if (resource == null) {
            return Optional.empty();
        }
        final ExternalResourceEntity entity = DomainToEntityMapper.toEntity(resource);
        final ExternalResourceEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public Optional<Integer> update(final ExternalResource resource) {
        if (resource == null || resource.getId() == null) {
            return Optional.empty();
        }

        final Optional<ExternalResourceEntity> existing = repository.findById(resource.getId());
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        final ExternalResourceEntity entity = existing.get();
        DomainToEntityMapper.applyToEntity(resource, entity);

        final ExternalResourceEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }
}

