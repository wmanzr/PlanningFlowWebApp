package RUT.PlanningFlow.application.service.notification;

import RUT.PlanningFlow.application.dto.notification.RealtimeEventType;
import RUT.PlanningFlow.application.dto.notification.RealtimeMessage;
import RUT.PlanningFlow.application.port.out.NotificationPublisherPort;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.NotificationRepositoryPort;
import RUT.PlanningFlow.domain.enums.IncidentSeverity;
import RUT.PlanningFlow.domain.model.Assignment;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.time.LocalDateTime;

@Service
public class NotificationService {

    private static final int DESCRIPTION_PREVIEW_MAX = 280;

    private final NotificationPublisherPort notificationPublisher;
    private final Gson gson;
    private final EventRepositoryPort eventRepository;
    private final NotificationRepositoryPort notificationRepository;

    public NotificationService(
            final NotificationPublisherPort notificationPublisher,
            final Gson gson,
            final EventRepositoryPort eventRepository,
            final NotificationRepositoryPort notificationRepository
    ) {
        this.notificationPublisher = notificationPublisher;
        this.gson = gson;
        this.eventRepository = eventRepository;
        this.notificationRepository = notificationRepository;
    }

    public void notifyParticipantAssignmentCreated(final User assignee, final Task task, final int assignmentId) {
        if (assignee.getId() == null || task.getEvent() == null || task.getEvent().getId() == null) {
            return;
        }
        final LocalDateTime now = LocalDateTime.now();
        final String title = "Назначение на задачу";
        final String messageText = "Вас назначили на задачу «" + safe(task.getTitle()) + "».";
        final Integer notificationId = notificationRepository.create(assignee.getId(), title, messageText, now);
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("notificationId", notificationId);
        payload.put("assignmentId", assignmentId);
        payload.put("taskId", task.getId());
        payload.put("eventId", task.getEvent().getId());
        payload.put("taskTitle", task.getTitle());
        payload.put("title", title);
        payload.put("message", messageText);
        payload.put("createdAt", now.toString());
        notificationPublisher.publishToUser(assignee.getId(), message(RealtimeEventType.ASSIGNMENT_ASSIGNED, payload));
    }

    
    public void notifyParticipantAssignmentRemoved(final User assignee, final Task task, final Integer assignmentId) {
        if (assignee.getId() == null || task.getEvent() == null || task.getEvent().getId() == null) {
            return;
        }
        final LocalDateTime now = LocalDateTime.now();
        final String title = "Снятие с задачи";
        final String messageText = "Вас сняли с задачи «" + safe(task.getTitle()) + "».";
        final Integer notificationId = notificationRepository.create(assignee.getId(), title, messageText, now);
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("notificationId", notificationId);
        payload.put("assignmentId", assignmentId);
        payload.put("taskId", task.getId());
        payload.put("eventId", task.getEvent().getId());
        payload.put("taskTitle", task.getTitle());
        payload.put("title", title);
        payload.put("message", messageText);
        payload.put("createdAt", now.toString());
        notificationPublisher.publishToUser(assignee.getId(), message(RealtimeEventType.ASSIGNMENT_REMOVED, payload));
    }

    public void notifyUserAppointedEventCoordinator(final User assignee, final Event event) {
        if (assignee.getId() == null || event.getId() == null) {
            return;
        }
        final LocalDateTime now = LocalDateTime.now();
        final String title = "Назначение координатором";
        final String messageText = "Вас назначили координатором мероприятия «" + safe(event.getTitle()) + "».";
        final Integer notificationId = notificationRepository.create(assignee.getId(), title, messageText, now);
        if (notificationId == null) {
            return;
        }
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("notificationId", notificationId);
        payload.put("eventId", event.getId());
        payload.put("eventTitle", event.getTitle());
        payload.put("title", title);
        payload.put("message", messageText);
        payload.put("createdAt", now.toString());
        notificationPublisher.publishToUser(
                assignee.getId(),
                message(RealtimeEventType.COORDINATOR_ASSIGNED, payload)
        );
    }

    public void notifyCoordinatorsAssignmentAccepted(final Assignment assignment, final User participant) {
        final List<Integer> recipients = withoutUser(coordinatorRecipientIds(assignment), participant.getId());
        if (recipients.isEmpty() || participant.getId() == null) {
            return;
        }
        final String taskTitle = assignment.getTask() != null ? assignment.getTask().getTitle() : "задача";
        final String title = "Назначение принято";
        final String who = participantDisplayName(participant);
        final String message =
                who + " принял(а) назначение на задачу «" + safe(taskTitle) + "».";
        final Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("assignmentId", assignment.getId());
        extra.put("participantId", participant.getId());
        extra.put("participantUsername", participant.getUsername());
        extra.put("participantDisplayName", who);
        publishPersistedUserNotifications(recipients, RealtimeEventType.ASSIGNMENT_ACCEPTED, title, message, extra);
    }

    public void notifyCoordinatorsAssignmentRejected(
            final Assignment assignment,
            final User participant,
            final String reason
    ) {
        final List<Integer> recipients = withoutUser(coordinatorRecipientIds(assignment), participant.getId());
        if (recipients.isEmpty() || participant.getId() == null) {
            return;
        }
        final String taskTitle = assignment.getTask() != null ? assignment.getTask().getTitle() : "задача";
        final String title = "Отказ от назначения";
        final String who = participantDisplayName(participant);
        final String message =
                who + " отказался(ась) от задачи «" + safe(taskTitle) + "». Причина: "
                        + safe(reason);
        final Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("assignmentId", assignment.getId());
        extra.put("participantId", participant.getId());
        extra.put("participantUsername", participant.getUsername());
        extra.put("participantDisplayName", who);
        extra.put("reason", reason == null ? "" : reason);
        publishPersistedUserNotifications(recipients, RealtimeEventType.ASSIGNMENT_REJECTED, title, message, extra);
    }

    public void notifyCoordinatorsIncidentReported(
            final Integer reporterUserId,
            final int eventId,
            final int incidentId,
            final IncidentSeverity severity,
            final String description
    ) {
        final Event event = eventRepository.findById(eventId).orElse(null);
        final List<Integer> recipients = withoutUser(coordinatorRecipientUserIds(event), reporterUserId);
        if (recipients.isEmpty()) {
            return;
        }
        final String title = "Новый инцидент";
        final String message = "На мероприятии «" + safe(event != null ? event.getTitle() : "") + "» зарегистрирован инцидент, критичность: "
                + severityRu(severity) + ". " + preview(description);
        final Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("eventId", eventId);
        extra.put("incidentId", incidentId);
        extra.put("severity", severity != null ? severity.name() : null);
        extra.put("descriptionPreview", preview(description));
        publishPersistedUserNotifications(recipients, RealtimeEventType.INCIDENT_REPORTED, title, message, extra);
    }

    private List<Integer> coordinatorRecipientIds(final Assignment assignment) {
        return coordinatorRecipientUserIds(resolveEvent(assignment));
    }

    private static List<Integer> coordinatorRecipientUserIds(final Event event) {
        if (event == null) {
            return List.of();
        }
        final LinkedHashSet<Integer> ids = new LinkedHashSet<>();
        if (event.getCreator() != null && event.getCreator().getId() != null) {
            ids.add(event.getCreator().getId());
        }
        if (event.getCoordinators() != null) {
            for (final User u : event.getCoordinators()) {
                if (u != null && u.getId() != null) {
                    ids.add(u.getId());
                }
            }
        }
        return List.copyOf(ids);
    }

    private Event resolveEvent(final Assignment assignment) {
        if (assignment.getTask() == null || assignment.getTask().getEvent() == null) {
            return null;
        }
        final Integer eventId = assignment.getTask().getEvent().getId();
        if (eventId == null) {
            return assignment.getTask().getEvent();
        }
        return eventRepository.findById(eventId).orElse(assignment.getTask().getEvent());
    }

    private RealtimeMessage message(final RealtimeEventType type, final Map<String, Object> payload) {
        return new RealtimeMessage(type.wireValue(), System.currentTimeMillis(), gson.toJson(payload));
    }

    private static String preview(final String description) {
        if (description == null || description.isBlank()) {
            return "";
        }
        final String t = description.trim();
        return t.length() <= DESCRIPTION_PREVIEW_MAX ? t : t.substring(0, DESCRIPTION_PREVIEW_MAX) + "…";
    }

    private static String safe(final String value) {
        return value == null ? "" : value.trim();
    }

    
    private static String participantDisplayName(final User participant) {
        if (participant == null) {
            return "";
        }
        final String full = participant.getFullName();
        if (full == null || full.isBlank()) {
            return safe(participant.getUsername());
        }
        final String[] parts = full.trim().split("\\s+");
        if (parts.length == 0) {
            return safe(participant.getUsername());
        }
        final String last = parts[0];
        final String first =
                parts.length > 1 && !parts[1].isEmpty() ? parts[1].substring(0, 1) + "." : "";
        final String middle =
                parts.length > 2 && !parts[2].isEmpty() ? parts[2].substring(0, 1) + "." : "";
        final String initials = first + middle;
        return initials.isEmpty() ? last : last + " " + initials;
    }

    private void publishPersistedUserNotifications(
            final List<Integer> recipientIds,
            final RealtimeEventType eventType,
            final String title,
            final String messageBody,
            final Map<String, Object> extraPayloadFields
    ) {
        if (recipientIds == null || recipientIds.isEmpty()) {
            return;
        }
        final LocalDateTime now = LocalDateTime.now();
        for (final Integer userId : recipientIds) {
            if (userId == null || userId <= 0) {
                continue;
            }
            final Integer notificationId = notificationRepository.create(userId, title, messageBody, now);
            if (notificationId == null) {
                continue;
            }
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("notificationId", notificationId);
            payload.put("title", title);
            payload.put("message", messageBody);
            payload.put("createdAt", now.toString());
            if (extraPayloadFields != null && !extraPayloadFields.isEmpty()) {
                payload.putAll(extraPayloadFields);
            }
            notificationPublisher.publishToUser(userId, message(eventType, payload));
        }
    }

    private static List<Integer> withoutUser(final List<Integer> recipients, final Integer userId) {
        if (recipients == null || recipients.isEmpty()) {
            return List.of();
        }
        if (userId == null) {
            return List.copyOf(recipients);
        }
        return recipients.stream()
                .filter(Objects::nonNull)
                .filter(id -> !userId.equals(id))
                .toList();
    }

    private static String severityRu(final IncidentSeverity severity) {
        if (severity == null) {
            return "не указана";
        }
        return switch (severity) {
            case LOW -> "низкая";
            case MEDIUM -> "средняя";
            case HIGH -> "высокая";
            case CRITICAL -> "критическая";
        };
    }
}