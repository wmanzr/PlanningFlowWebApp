package RUT.PlanningFlow.application.service.event;

import RUT.PlanningFlow.application.port.out.AIPort;
import RUT.PlanningFlow.application.port.out.repository.EventAiPostmortemReportRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class EventPostMortemAiProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(EventPostMortemAiProcessor.class);

    private final EventRepositoryPort eventRepository;
    private final EventAiPostmortemReportRepositoryPort reportRepository;
    private final EventAiContextAggregationService aggregationService;
    private final AIPort aiPort;

    public EventPostMortemAiProcessor(
            final EventRepositoryPort eventRepository,
            final EventAiPostmortemReportRepositoryPort reportRepository,
            final EventAiContextAggregationService aggregationService,
            final AIPort aiPort
    ) {
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(reportRepository, "Репозиторий отчетов ИИ обязателен", "EVENT_AI_REPORT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(aggregationService, "Сервис агрегации контекста обязателен", "EVENT_AI_AGGREGATION_REQUIRED");
        DomainAssert.notNull(aiPort, "AI порт обязателен", "AI_PORT_REQUIRED");
        this.eventRepository = eventRepository;
        this.reportRepository = reportRepository;
        this.aggregationService = aggregationService;
        this.aiPort = aiPort;
    }

    public void completeReportFromSnapshot(final Integer eventId) {
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        final Optional<Event> ev = eventRepository.findById(eventId);
        if (ev.isEmpty() || ev.get().getStatus() != EventStatus.COMPLETED) {
            LOG.debug("Пропуск генерации отчета ИИ: мероприятие {} не найдено или не в статусе COMPLETED", eventId);
            return;
        }
        LOG.info("Старт фоновой генерации отчета ИИ для мероприятия {}", eventId);
        try {
            final String json = aggregationService.buildJsonSnapshot(eventId);
            final String userContent = "Данные мероприятия в формате JSON (UTF-8):\n" + json;
            final String report = aiPort.complete(EventPostMortemAiPrompts.POST_MORTEM_SYSTEM_PROMPT, userContent);
            reportRepository.updateCompleted(eventId, report == null ? "" : report);
            LOG.info("Отчет ИИ для мероприятия {} успешно сохранен", eventId);
        } catch (final Exception e) {
            LOG.warn("Не удалось сформировать отчет ИИ для мероприятия {}", eventId, e);
            final String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            reportRepository.updateFailed(eventId, msg);
        }
    }
}
