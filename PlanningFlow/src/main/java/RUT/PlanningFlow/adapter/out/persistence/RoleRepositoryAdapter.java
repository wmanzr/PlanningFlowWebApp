package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.RoleEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.RoleRepository;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.out.repository.RoleRepositoryPort;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final RoleRepository repository;

    public RoleRepositoryAdapter(final RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Role> findByName(final UserRoles name) {
        if (name == null) {
            return Optional.empty();
        }
        return repository.findByName(name).map(EntityToDomainMapper::toDomain);
    }

    @Override
    public Optional<Role> findById(final Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id).map(EntityToDomainMapper::toDomain);
    }

    @Override
    public List<Role> findAll() {
        final List<RoleEntity> entities = repository.findAll();
        final List<Role> items = new ArrayList<>(entities.size());
        for (final RoleEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public PageResult<Role> findRoles(final PageQuery pageQuery) {
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<RoleEntity> page = repository.findAllByOrderByNameAscIdAsc(pageable);

        final List<Role> items = new ArrayList<>(page.getContent().size());
        for (final RoleEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Role> findByNameContainingIgnoreCase(final String searchTerm, final PageQuery pageQuery) {
        final String normalized = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
        if (normalized.isBlank()) {
            return findRoles(pageQuery);
        }

        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<RoleEntity> page = repository.findByNameContainingIgnoreCaseOrderByNameAscIdAsc(normalized, pageable);

        final List<Role> items = new ArrayList<>(page.getContent().size());
        for (final RoleEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public Optional<Integer> create(final Role role) {
        if (role == null) {
            return Optional.empty();
        }
        final RoleEntity entity = DomainToEntityMapper.toEntity(role);
        final RoleEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public Optional<Integer> update(final Role role) {
        if (role == null || role.getId() == null) {
            return Optional.empty();
        }

        final Optional<RoleEntity> existing = repository.findById(role.getId());
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        final RoleEntity entity = existing.get();
        DomainToEntityMapper.applyToEntity(role, entity);

        final RoleEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }
}