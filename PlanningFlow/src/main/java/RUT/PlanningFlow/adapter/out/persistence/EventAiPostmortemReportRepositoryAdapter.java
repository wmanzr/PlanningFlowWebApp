package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.EventAiPostmortemReportEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.EventEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.EventAiPostmortemReportRepository;
import RUT.PlanningFlow.application.dto.event.EventPostMortemAiReportResponseDto;
import RUT.PlanningFlow.application.port.out.repository.EventAiPostmortemReportRepositoryPort;
import RUT.PlanningFlow.domain.enums.EventAiPostmortemReportStatus;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Repository
public class EventAiPostmortemReportRepositoryAdapter implements EventAiPostmortemReportRepositoryPort {

    private final EventAiPostmortemReportRepository repository;
    private final EntityManager entityManager;

    public EventAiPostmortemReportRepositoryAdapter(
            final EventAiPostmortemReportRepository repository,
            final EntityManager entityManager
    ) {
        DomainAssert.notNull(repository, "Репозиторий отчетов ИИ обязателен", "EVENT_AI_REPORT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(entityManager, "EntityManager обязателен", "ENTITY_MANAGER_REQUIRED");
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upsertPending(final Integer eventId) {
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Optional<EventAiPostmortemReportEntity> existing = repository.findByEvent_Id(eventId);
        if (existing.isEmpty()) {
            final EventAiPostmortemReportEntity e = new EventAiPostmortemReportEntity();
            e.setEvent(entityManager.getReference(EventEntity.class, eventId));
            e.setStatus(EventAiPostmortemReportStatus.PENDING);
            e.setReportText(null);
            e.setErrorMessage(null);
            e.setCreatedAt(now);
            e.setUpdatedAt(now);
            repository.save(e);
            return;
        }
        final EventAiPostmortemReportEntity e = existing.get();
        e.setStatus(EventAiPostmortemReportStatus.PENDING);
        e.setReportText(null);
        e.setErrorMessage(null);
        e.setUpdatedAt(now);
        repository.save(e);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateCompleted(final Integer eventId, final String reportText) {
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        final EventAiPostmortemReportEntity e = repository.findByEvent_Id(eventId)
                .orElseThrow(() -> new IllegalStateException("Отчет ИИ для мероприятия не найден: " + eventId));
        e.setStatus(EventAiPostmortemReportStatus.COMPLETED);
        e.setReportText(reportText);
        e.setErrorMessage(null);
        e.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        repository.save(e);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFailed(final Integer eventId, final String errorMessage) {
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        final Optional<EventAiPostmortemReportEntity> opt = repository.findByEvent_Id(eventId);
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (opt.isEmpty()) {
            final EventAiPostmortemReportEntity e = new EventAiPostmortemReportEntity();
            e.setEvent(entityManager.getReference(EventEntity.class, eventId));
            e.setStatus(EventAiPostmortemReportStatus.FAILED);
            e.setReportText(null);
            e.setErrorMessage(trimError(errorMessage));
            e.setCreatedAt(now);
            e.setUpdatedAt(now);
            repository.save(e);
            return;
        }
        final EventAiPostmortemReportEntity e = opt.get();
        e.setStatus(EventAiPostmortemReportStatus.FAILED);
        e.setReportText(null);
        e.setErrorMessage(trimError(errorMessage));
        e.setUpdatedAt(now);
        repository.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventPostMortemAiReportResponseDto> findResponseByEventId(final Integer eventId) {
        if (eventId == null) {
            return Optional.empty();
        }
        return repository.findByEvent_Id(eventId).map(EventAiPostmortemReportRepositoryAdapter::toDto);
    }

    private static EventPostMortemAiReportResponseDto toDto(final EventAiPostmortemReportEntity e) {
        return new EventPostMortemAiReportResponseDto(
                e.getStatus() != null ? e.getStatus().name() : null,
                e.getReportText(),
                e.getErrorMessage(),
                e.getUpdatedAt() != null ? e.getUpdatedAt().toString() : null
        );
    }

    private static String trimError(final String errorMessage) {
        if (errorMessage == null) {
            return null;
        }
        if (errorMessage.length() <= 4000) {
            return errorMessage;
        }
        return errorMessage.substring(0, 4000) + "…";
    }
}
