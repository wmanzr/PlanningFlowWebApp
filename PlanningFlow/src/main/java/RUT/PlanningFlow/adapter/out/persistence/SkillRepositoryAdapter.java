package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.SkillEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.SkillRepository;
import RUT.PlanningFlow.adapter.out.persistence.repository.UserSkillRepository;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.out.repository.SkillRepositoryPort;
import RUT.PlanningFlow.domain.model.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class SkillRepositoryAdapter implements SkillRepositoryPort {

    private final SkillRepository repository;
    private final UserSkillRepository userSkillRepository;

    public SkillRepositoryAdapter(
            final SkillRepository repository,
            final UserSkillRepository userSkillRepository
    ) {
        this.repository = repository;
        this.userSkillRepository = userSkillRepository;
    }

    @Override
    public Optional<Skill> findById(final Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id).map(EntityToDomainMapper::toDomain);
    }

    @Override
    public List<String> findDistinctCategories() {
        final List<String> categories = repository.findDistinctCategoriesOrderByCategoryAsc();
        return List.copyOf(categories);
    }

    @Override
    public boolean existsByNameIgnoreCase(final String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return repository.existsByNameIgnoreCase(name.trim());
    }

    @Override
    public PageResult<Skill> findSkillsOrderByCategoryAscNameAsc(final PageQuery pageQuery) {
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<SkillEntity> page = repository.findCatalogOrderedByCategoryThenName(pageable);

        final List<Skill> items = new ArrayList<>(page.getContent().size());
        for (final SkillEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public List<Skill> findAll() {
        final List<SkillEntity> entities = repository.findAll();
        final List<Skill> items = new ArrayList<>(entities.size());
        for (final SkillEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public PageResult<Skill> findSkills(final PageQuery pageQuery) {
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<SkillEntity> page = repository.findAllByOrderByNameAscIdAsc(pageable);

        final List<Skill> items = new ArrayList<>(page.getContent().size());
        for (final SkillEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Skill> findByNameContainingIgnoreCase(final String searchTerm, final PageQuery pageQuery) {
        final String normalized = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
        if (normalized.isBlank()) {
            return findSkills(pageQuery);
        }

        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<SkillEntity> page = repository.findByNameContainingIgnoreCaseOrderByNameAscIdAsc(normalized, pageable);

        final List<Skill> items = new ArrayList<>(page.getContent().size());
        for (final SkillEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public Optional<Integer> create(final Skill skill) {
        if (skill == null) {
            return Optional.empty();
        }
        final SkillEntity entity = DomainToEntityMapper.toEntity(skill);
        final SkillEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public Optional<Integer> update(final Skill skill) {
        if (skill == null || skill.getId() == null) {
            return Optional.empty();
        }

        final Optional<SkillEntity> existing = repository.findById(skill.getId());
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        final SkillEntity entity = existing.get();
        DomainToEntityMapper.applyToEntity(skill, entity);

        final SkillEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public boolean deleteCatalogEntry(final Integer skillId) {
        if (skillId == null) {
            return false;
        }
        final Optional<SkillEntity> existing = repository.findById(skillId);
        if (existing.isEmpty()) {
            return false;
        }
        userSkillRepository.deleteAllForSkillId(skillId);
        repository.deleteTaskRequiredSkillLinksBySkillId(skillId);
        repository.delete(existing.get());
        return true;
    }
}

