package RUT.PlanningFlow.application.service.incident;

import RUT.PlanningFlow.application.port.in.incident.ReportIncidentUseCase;
import RUT.PlanningFlow.application.service.notification.NotificationService;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.IncidentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.ResourceRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.domain.enums.IncidentSeverity;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Incident;
import RUT.PlanningFlow.domain.model.Resource;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class ReportIncidentService implements ReportIncidentUseCase {

    private final IncidentRepositoryPort incidentRepository;
    private final EventRepositoryPort eventRepository;
    private final TaskRepositoryPort taskRepository;
    private final UserRepositoryPort userRepository;
    private final ResourceRepositoryPort resourceRepository;
    private final NotificationService realtimeNotifications;

    public ReportIncidentService(
            final IncidentRepositoryPort incidentRepository,
            final EventRepositoryPort eventRepository,
            final TaskRepositoryPort taskRepository,
            final UserRepositoryPort userRepository,
            final ResourceRepositoryPort resourceRepository,
            final NotificationService realtimeNotifications
    ) {
        DomainAssert.notNull(incidentRepository, "Репозиторий инцидентов обязателен", "INCIDENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(resourceRepository, "Репозиторий ресурсов обязателен", "RESOURCE_REPOSITORY_REQUIRED");
        DomainAssert.notNull(realtimeNotifications, "Уведомления обязательны", "REALTIME_NOTIFICATIONS_REQUIRED");
        this.incidentRepository = incidentRepository;
        this.eventRepository = eventRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.realtimeNotifications = realtimeNotifications;
    }

    @Override
    public Integer execute(
            final Integer reporterId,
            final Integer eventId,
            final Integer taskId,
            final Integer resourceId,
            final String description,
            final IncidentSeverity severity
    ) {
        DomainAssert.notNull(reporterId, "ID инициатора обязателен", "INCIDENT_REPORTER_REQUIRED");
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        DomainAssert.notBlank(description, "Описание инцидента обязательно", "INCIDENT_DESCRIPTION_REQUIRED");
        DomainAssert.notNull(severity, "Критичность инцидента обязательна", "INCIDENT_SEVERITY_REQUIRED");

        final User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new DomainException("Инициатор инцидента не найден", "INCIDENT_REPORTER_NOT_FOUND"));
        final Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new DomainException("Мероприятие не найдено", "EVENT_NOT_FOUND"));
        event.assertAllowsReportingIncidents();

        final Optional<Task> exTask = taskId == null ? Optional.empty() : taskRepository.findById(taskId);
        final Task task = exTask.orElse(null);
        if (task != null && task.getEvent() != null && task.getEvent().getId() != null && !task.getEvent().getId().equals(eventId)) {
            throw new DomainException("Задача должна относиться к указанному мероприятию", "TASK_WRONG_EVENT");
        }
        if (task != null) {
            task.assertEligibleForNewIncidentAttachment();
        }

        final Optional<Resource> exResource = resourceId == null ? Optional.empty() : resourceRepository.findById(resourceId);
        final Resource resource = exResource.orElse(null);

        final Incident incident = new Incident(
                null,
                event,
                task,
                resource,
                reporter,
                description,
                severity,
                IncidentStatus.OPEN,
                LocalDateTime.now(),
                null,
                null
        );
        final Integer incidentId = incidentRepository.create(incident)
                .orElseThrow(() -> new DomainException("Не удалось зарегистрировать инцидент", "INCIDENT_CREATE_FAILED"));
        realtimeNotifications.notifyCoordinatorsIncidentReported(reporter.getId(), eventId, incidentId, severity, description);
        return incidentId;
    }
}