package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.IncidentEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.IncidentRepository;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.out.repository.IncidentRepositoryPort;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import RUT.PlanningFlow.domain.model.Incident;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class IncidentRepositoryAdapter implements IncidentRepositoryPort {

    private final IncidentRepository repository;

    public IncidentRepositoryAdapter(final IncidentRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Incident> findOpenOrInProgressByEventId(final Integer eventId) {
        if (eventId == null) {
            return List.of();
        }
        final List<IncidentEntity> entities = repository.findByEvent_IdAndStatusInOrderByIdAsc(
                eventId,
                List.of(IncidentStatus.OPEN, IncidentStatus.IN_PROGRESS)
        );
        final List<Incident> items = new ArrayList<>(entities.size());
        for (final IncidentEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public Optional<Incident> findById(final Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id).map(EntityToDomainMapper::toDomain);
    }

    @Override
    public List<Incident> findAll() {
        final List<IncidentEntity> entities = repository.findAll();
        final List<Incident> items = new ArrayList<>(entities.size());
        for (final IncidentEntity e : entities) {
            items.add(EntityToDomainMapper.toDomain(e));
        }
        return List.copyOf(items);
    }

    @Override
    public PageResult<Incident> findIncidents(final PageQuery pageQuery) {
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<IncidentEntity> page = repository.findAllByOrderByIdDesc(pageable);

        final List<Incident> items = new ArrayList<>(page.getContent().size());
        for (final IncidentEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }

        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Incident> findByEventId(final Integer eventId, final PageQuery pageQuery) {
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");

        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<IncidentEntity> page = repository.findByEvent_IdOrderByCreatedAtDescIdDesc(eventId, pageable);

        final List<Incident> items = new ArrayList<>(page.getContent().size());
        for (final IncidentEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }

        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Incident> findByDescriptionContainingIgnoreCase(final String searchTerm, final PageQuery pageQuery) {
        final String normalized = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
        if (normalized.isBlank()) {
            return findIncidents(pageQuery);
        }

        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final Page<IncidentEntity> page = repository.findByDescriptionContainingIgnoreCaseOrderByIdDesc(normalized, pageable);

        final List<Incident> items = new ArrayList<>(page.getContent().size());
        for (final IncidentEntity e : page.getContent()) {
            items.add(EntityToDomainMapper.toDomain(e));
        }

        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public long countByStatus(final IncidentStatus status) {
        if (status == null) {
            return 0L;
        }
        return repository.countByStatus(status);
    }

    @Override
    public Optional<Integer> create(final Incident incident) {
        if (incident == null) {
            return Optional.empty();
        }
        final IncidentEntity entity = DomainToEntityMapper.toEntity(incident);
        final IncidentEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }

    @Override
    public Optional<Integer> update(final Incident incident) {
        if (incident == null || incident.getId() == null) {
            return Optional.empty();
        }

        final Optional<IncidentEntity> existing = repository.findById(incident.getId());
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        final IncidentEntity entity = existing.get();
        DomainToEntityMapper.applyToEntity(incident, entity);

        final IncidentEntity saved = repository.save(entity);
        return Optional.of(saved.getId());
    }
}