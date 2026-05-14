package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.UserSkillEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.UserSkillRepository;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.out.repository.UserSkillRepositoryPort;
import RUT.PlanningFlow.domain.model.UserSkill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserSkillRepositoryAdapter implements UserSkillRepositoryPort {

    private final UserSkillRepository repository;

    public UserSkillRepositoryAdapter(final UserSkillRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserSkill> findById(final Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id).map(EntityToDomainMapper::toDomain);
    }

    @Override
    public List<UserSkill> findAll() {
        final List<UserSkillEntity> entities = repository.findAll();
        final List<UserSkill> items = new ArrayList<>(entities.size());
        for (final UserSkillEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public PageResult<UserSkill> findUserSkills(final PageQuery pageQuery) {
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<UserSkillEntity> page = repository.findAllByOrderByVerifiedAtDescIdDesc(pageable);

        final List<UserSkill> items = new ArrayList<>(page.getContent().size());
        for (final UserSkillEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<UserSkill> findBySkillNameContainingIgnoreCase(final String searchTerm, final PageQuery pageQuery) {
        final String normalized = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
        if (normalized.isBlank()) {
            return findUserSkills(pageQuery);
        }

        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<UserSkillEntity> page = repository.findBySkill_NameContainingIgnoreCaseOrderByVerifiedAtDescIdDesc(
                normalized,
                pageable
        );

        final List<UserSkill> items = new ArrayList<>(page.getContent().size());
        for (final UserSkillEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public List<UserSkill> findForUser(final Integer userId) {
        if (userId == null) {
            return List.of();
        }
        final List<UserSkillEntity> entities = repository.findByUser_IdOrderByVerifiedAtDescIdDesc(userId);
        final List<UserSkill> items = new ArrayList<>(entities.size());
        for (final UserSkillEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public Optional<UserSkill> findByUserIdAndSkillId(final Integer userId, final Integer skillId) {
        if (userId == null || skillId == null) {
            return Optional.empty();
        }
        return repository.findByUser_IdAndSkill_Id(userId, skillId).map(EntityToDomainMapper::toDomain);
    }

    @Override
    public Optional<Integer> create(final UserSkill userSkill) {
        if (userSkill == null) {
            return Optional.empty();
        }
        final UserSkillEntity entity = DomainToEntityMapper.toEntity(userSkill);
        final UserSkillEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public Optional<Integer> update(final UserSkill userSkill) {
        if (userSkill == null || userSkill.getId() == null) {
            return Optional.empty();
        }

        final Optional<UserSkillEntity> existing = repository.findById(userSkill.getId());
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        final UserSkillEntity entity = existing.get();
        DomainToEntityMapper.applyToEntity(userSkill, entity);

        final UserSkillEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public void deleteById(final Integer id) {
        if (id == null) {
            return;
        }
        repository.deleteById(id);
    }
}