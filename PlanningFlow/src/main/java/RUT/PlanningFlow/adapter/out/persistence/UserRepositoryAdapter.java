package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.UserEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.UserSkillEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.UserRepository;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.model.UserSkill;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository repository;

    public UserRepositoryAdapter(final UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findByUsername(final String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return repository.findByUsername(username.trim()).map(EntityToDomainMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmailIgnoreCase(final String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return repository.findByEmailIgnoreCase(email.trim()).map(EntityToDomainMapper::toDomain);
    }

    @Override
    public Optional<User> findById(final Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id).map(EntityToDomainMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        final List<UserEntity> entities = repository.findAll();
        final List<User> items = new ArrayList<>(entities.size());
        for (final UserEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public PageResult<User> findUsers(final PageQuery pageQuery) {
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<UserEntity> page = repository.findAllByOrderByUsernameAscIdAsc(pageable);

        final List<User> items = new ArrayList<>(page.getContent().size());
        for (final UserEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<User> findHavingRolesAndOptionalUsername(
            final List<UserRoles> roles,
            final String usernameTermOrNull,
            final PageQuery pageQuery
    ) {
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        if (roles == null || roles.isEmpty()) {
            return new PageResult<>(List.of(), 0, 0);
        }
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final String term = usernameTermOrNull == null || usernameTermOrNull.isBlank()
                ? null
                : usernameTermOrNull.trim();
        final Page<UserEntity> page = term == null
                ? repository.findHavingRoles(roles, pageable)
                : repository.findHavingRolesAndUsernameContaining(roles, term, pageable);
        final List<User> items = new ArrayList<>(page.getContent().size());
        for (final UserEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<User> findUsersHavingRole(final UserRoles role, final PageQuery pageQuery) {
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        DomainAssert.notNull(role, "Роль обязательна", "ROLE_REQUIRED");
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<UserEntity> page = repository.findUsersHavingRole(role, pageable);
        final List<User> items = new ArrayList<>(page.getContent().size());
        for (final UserEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<User> findUsersHavingRoleAndUsernameContaining(
            final UserRoles role,
            final String term,
            final PageQuery pageQuery
    ) {
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        DomainAssert.notNull(role, "Роль обязательна", "ROLE_REQUIRED");
        final String needle = term == null || term.isBlank() ? "" : term.trim();
        if (needle.isEmpty()) {
            return findUsersHavingRole(role, pageQuery);
        }
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<UserEntity> page = repository.findUsersHavingRoleAndUsernameContaining(role, needle, pageable);
        final List<User> items = new ArrayList<>(page.getContent().size());
        for (final UserEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<User> findHavingDirectoryRoleAndFilterRole(
            final List<UserRoles> directoryRoles,
            final UserRoles filterRole,
            final PageQuery pageQuery
    ) {
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        if (directoryRoles == null || directoryRoles.isEmpty() || filterRole == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<UserEntity> page =
                repository.findHavingDirectoryRoleAndFilterRole(directoryRoles, filterRole, pageable);
        final List<User> items = new ArrayList<>(page.getContent().size());
        for (final UserEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<User> findHavingDirectoryRoleAndFilterRoleAndUsernameContaining(
            final List<UserRoles> directoryRoles,
            final UserRoles filterRole,
            final String term,
            final PageQuery pageQuery
    ) {
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        if (directoryRoles == null || directoryRoles.isEmpty() || filterRole == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        final String needle = term == null || term.isBlank() ? "" : term.trim();
        if (needle.isEmpty()) {
            return findHavingDirectoryRoleAndFilterRole(directoryRoles, filterRole, pageQuery);
        }
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<UserEntity> page = repository.findHavingDirectoryRoleAndFilterRoleAndUsernameContaining(
                directoryRoles,
                filterRole,
                needle,
                pageable
        );
        final List<User> items = new ArrayList<>(page.getContent().size());
        for (final UserEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<User> findByUsernameContainingIgnoreCase(final String searchTerm, final PageQuery pageQuery) {
        final String needle = searchTerm == null ? "" : searchTerm.trim();
        if (needle.isBlank()) {
            return findUsers(pageQuery);
        }

        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<UserEntity> page =
                repository.findByUsernameContainingIgnoreCaseOrderByUsernameAscIdAsc(needle, pageable);

        final List<User> items = new ArrayList<>(page.getContent().size());
        for (final UserEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public List<User> findActiveParticipant() {
        final List<UserEntity> entities = repository.findActiveParticipant(UserRoles.PARTICIPANT);
        final List<User> items = new ArrayList<>(entities.size());
        for (final UserEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public List<UserSkill> findSkillsForUser(final Integer userId) {
        final List<UserSkillEntity> entities = repository.findSkillsForUser(userId);
        final List<UserSkill> items = new ArrayList<>(entities.size());
        for (final UserSkillEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public long countAllUsers() {
        return repository.countAllUsers();
    }

    @Override
    public Optional<Integer> create(final User user) {
        if (user == null) {
            return Optional.empty();
        }
        final UserEntity entity = DomainToEntityMapper.toEntity(user);
        final UserEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public Optional<Integer> update(final User user) {
        if (user == null || user.getId() == null) {
            return Optional.empty();
        }

        final Optional<UserEntity> existing = repository.findById(user.getId());
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        final UserEntity entity = existing.get();
        DomainToEntityMapper.applyToEntity(user, entity);

        final UserEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }
}