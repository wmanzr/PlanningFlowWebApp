package RUT.PlanningFlow.application.service.event;

import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.IncidentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.ResourceBookingRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.domain.enums.IncidentSeverity;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Assignment;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.ExternalResource;
import RUT.PlanningFlow.domain.model.Incident;
import RUT.PlanningFlow.domain.model.Resource;
import RUT.PlanningFlow.domain.model.ResourceBooking;
import RUT.PlanningFlow.domain.model.Role;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.model.UserSkill;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@Service
@Transactional(readOnly = true)
public class EventAiContextAggregationService {

    public static final String SNAPSHOT_SCHEMA = "planningflow.event_snapshot.v3";

    private static final PageQuery LARGE_PAGE = new PageQuery(1, 5000);

    private static final int MAX_DESCRIPTION = 4000;
    private static final int MAX_INCIDENT_DESCRIPTION = 2000;
    private static final int MAX_RESOLUTION_NOTES = 1500;

    private final EventRepositoryPort eventRepository;
    private final TaskRepositoryPort taskRepository;
    private final AssignmentRepositoryPort assignmentRepository;
    private final IncidentRepositoryPort incidentRepository;
    private final ResourceBookingRepositoryPort resourceBookingRepository;
    private final UserRepositoryPort userRepository;
    private final Gson gson;

    public EventAiContextAggregationService(
            final EventRepositoryPort eventRepository,
            final TaskRepositoryPort taskRepository,
            final AssignmentRepositoryPort assignmentRepository,
            final IncidentRepositoryPort incidentRepository,
            final ResourceBookingRepositoryPort resourceBookingRepository,
            final UserRepositoryPort userRepository,
            final Gson gson
    ) {
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(assignmentRepository, "Репозиторий назначений обязателен", "ASSIGNMENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(incidentRepository, "Репозиторий инцидентов обязателен", "INCIDENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(resourceBookingRepository, "Репозиторий бронирований обязателен", "RESOURCE_BOOKING_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(gson, "Gson обязателен", "GSON_REQUIRED");
        this.eventRepository = eventRepository;
        this.taskRepository = taskRepository;
        this.assignmentRepository = assignmentRepository;
        this.incidentRepository = incidentRepository;
        this.resourceBookingRepository = resourceBookingRepository;
        this.userRepository = userRepository;
        this.gson = gson;
    }

    
    public String buildJsonSnapshot(final Integer eventId) {
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        final Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new DomainException("Мероприятие не найдено", "EVENT_NOT_FOUND"));

        final List<Task> tasks = safeList(taskRepository.findTasksForEvent(eventId));
        final Map<Integer, String> taskTitleById = new LinkedHashMap<>();
        for (final Task t : tasks) {
            if (t != null && t.getId() != null) {
                taskTitleById.put(t.getId(), t.getTitle());
            }
        }

        final List<Incident> incidents = loadAllIncidentsEntities(eventId);
        final Map<Integer, User> participantsById = collectDistinctUsers(event, tasks, incidents);

        final Map<String, Object> root = new LinkedHashMap<>();
        root.put("schema", SNAPSHOT_SCHEMA);
        root.put("generatedAt", LocalDateTime.now(ZoneOffset.UTC).toString());
        root.put("event", toEventMap(event));

        final List<Map<String, Object>> taskMaps = new ArrayList<>(tasks.size());
        for (final Task task : tasks) {
            if (task == null || task.getId() == null) {
                continue;
            }
            taskMaps.add(toTaskMap(task, taskTitleById));
        }
        root.put("tasks", taskMaps);

        final List<Map<String, Object>> incidentMaps = new ArrayList<>(incidents.size());
        for (final Incident i : incidents) {
            if (i != null && i.getId() != null) {
                incidentMaps.add(toIncidentMap(i));
            }
        }
        root.put("incidents", incidentMaps);

        root.put("participants", buildParticipantMaps(participantsById));
        root.put("summary", buildSummary(event, tasks, incidents));

        return gson.toJson(root);
    }

    private List<Incident> loadAllIncidentsEntities(final Integer eventId) {
        final List<Incident> out = new ArrayList<>();
        int page = 1;
        while (true) {
            final PageResult<Incident> pageResult = incidentRepository.findByEventId(eventId, new PageQuery(page, LARGE_PAGE.size()));
            for (final Incident i : pageResult.items()) {
                if (i != null && i.getId() != null) {
                    out.add(i);
                }
            }
            if (page >= pageResult.totalPages() || pageResult.items().isEmpty()) {
                break;
            }
            page++;
        }
        return out;
    }

    private Map<Integer, User> collectDistinctUsers(
            final Event event,
            final List<Task> tasks,
            final List<Incident> incidents
    ) {
        final Map<Integer, User> out = new LinkedHashMap<>();
        addUser(out, event.getCreator());
        for (final User c : event.getCoordinators()) {
            addUser(out, c);
        }
        for (final Task t : tasks) {
            if (t == null) {
                continue;
            }
            addUser(out, t.getCreatedBy());
            for (final Assignment a : safeList(assignmentRepository.findByTaskId(t.getId()))) {
                if (a != null) {
                    addUser(out, a.getUser());
                }
            }
        }
        for (final Incident i : incidents) {
            if (i != null) {
                addUser(out, i.getReporter());
            }
        }
        return new TreeMap<>(out);
    }

    private static void addUser(final Map<Integer, User> out, final User user) {
        if (user != null && user.getId() != null) {
            out.putIfAbsent(user.getId(), user);
        }
    }

    private List<Map<String, Object>> buildParticipantMaps(final Map<Integer, User> byId) {
        final List<Map<String, Object>> list = new ArrayList<>();
        for (final User u : byId.values()) {
            list.add(toParticipantMap(u));
        }
        return list;
    }

    private Map<String, Object> toParticipantMap(final User user) {
        final Map<String, Object> m = new LinkedHashMap<>(userRef(user));
        final List<Map<String, Object>> skills = new ArrayList<>();
        for (final UserSkill us : safeList(userRepository.findSkillsForUser(user.getId()))) {
            if (us == null) {
                continue;
            }
            final Map<String, Object> sm = new LinkedHashMap<>();
            final Skill sk = us.getSkill();
            if (sk != null) {
                sm.put("skillId", sk.getId());
                sm.put("skillName", sk.getName());
                sm.put("skillCategory", sk.getCategory());
            }
            sm.put("tier", us.getTier() != null ? us.getTier().name() : null);
            sm.put("verifiedAt", us.getVerifiedAt() != null ? us.getVerifiedAt().toString() : null);
            skills.add(sm);
        }
        m.put("skills", skills);
        return m;
    }

    private static Map<String, Object> buildSummary(
            final Event event,
            final List<Task> tasks,
            final List<Incident> incidents
    ) {
        final Map<String, Object> s = new LinkedHashMap<>();
        s.put("taskCount", tasks.size());

        final EnumMap<TaskStatus, Integer> taskByStatus = new EnumMap<>(TaskStatus.class);
        for (final TaskStatus ts : TaskStatus.values()) {
            taskByStatus.put(ts, 0);
        }
        for (final Task t : tasks) {
            if (t != null && t.getStatus() != null) {
                taskByStatus.merge(t.getStatus(), 1, Integer::sum);
            }
        }
        final Map<String, Integer> taskStatusCounts = new LinkedHashMap<>();
        taskByStatus.forEach((k, v) -> taskStatusCounts.put(k.name(), v));
        s.put("tasksByStatus", taskStatusCounts);

        final EnumMap<IncidentStatus, Integer> incSt = new EnumMap<>(IncidentStatus.class);
        for (final IncidentStatus st : IncidentStatus.values()) {
            incSt.put(st, 0);
        }
        final EnumMap<IncidentSeverity, Integer> incSev = new EnumMap<>(IncidentSeverity.class);
        for (final IncidentSeverity sev : IncidentSeverity.values()) {
            incSev.put(sev, 0);
        }
        for (final Incident i : incidents) {
            if (i == null) {
                continue;
            }
            if (i.getStatus() != null) {
                incSt.merge(i.getStatus(), 1, Integer::sum);
            }
            if (i.getSeverity() != null) {
                incSev.merge(i.getSeverity(), 1, Integer::sum);
            }
        }
        final Map<String, Integer> incidentByStatus = new LinkedHashMap<>();
        incSt.forEach((k, v) -> incidentByStatus.put(k.name(), v));
        s.put("incidentsByStatus", incidentByStatus);
        final Map<String, Integer> incidentBySeverity = new LinkedHashMap<>();
        incSev.forEach((k, v) -> incidentBySeverity.put(k.name(), v));
        s.put("incidentsBySeverity", incidentBySeverity);
        s.put("incidentCount", incidents.size());

        s.put("eventStatus", event.getStatus() != null ? event.getStatus().name() : null);
        return s;
    }

    private Map<String, Object> toEventMap(final Event event) {
        final Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", event.getId());
        m.put("title", event.getTitle());
        m.put("status", event.getStatus() != null ? event.getStatus().name() : null);
        m.put("start", event.getStartDate() != null ? event.getStartDate().toString() : null);
        m.put("end", event.getEndDate() != null ? event.getEndDate().toString() : null);
        m.put("description", trimForAi(event.getDescription(), MAX_DESCRIPTION));
        m.put("creator", userRef(event.getCreator()));
        final List<Map<String, Object>> coord = new ArrayList<>();
        for (final User u : event.getCoordinators()) {
            final Map<String, Object> ref = userRef(u);
            if (ref != null) {
                coord.add(ref);
            }
        }
        m.put("coordinators", coord);
        if (event.getLatitude() != null && event.getLongitude() != null) {
            m.put("geo", Map.of("lat", event.getLatitude(), "lon", event.getLongitude()));
        }
        return m;
    }

    private Map<String, Object> toTaskMap(final Task task, final Map<Integer, String> taskTitleById) {
        final Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", task.getId());
        m.put("title", task.getTitle());
        m.put("status", task.getStatus() != null ? task.getStatus().name() : null);
        m.put("start", task.getStartTime() != null ? task.getStartTime().toString() : null);
        m.put("end", task.getEndTime() != null ? task.getEndTime().toString() : null);
        m.put("createdBy", userRef(task.getCreatedBy()));

        if (task.getLatitude() != null && task.getLongitude() != null) {
            m.put("geo", Map.of("lat", task.getLatitude(), "lon", task.getLongitude()));
        }

        final List<Map<String, Object>> depDetails = new ArrayList<>();
        for (final Task d : task.getDependencies()) {
            if (d == null || d.getId() == null) {
                continue;
            }
            final Map<String, Object> dm = new LinkedHashMap<>();
            dm.put("id", d.getId());
            dm.put("title", d.getTitle() != null ? d.getTitle() : taskTitleById.get(d.getId()));
            depDetails.add(dm);
        }
        m.put("dependencies", depDetails);

        final List<Map<String, Object>> reqSkills = new ArrayList<>();
        for (final Skill sk : task.getRequiredSkills()) {
            if (sk == null) {
                continue;
            }
            final Map<String, Object> sm = new LinkedHashMap<>();
            sm.put("id", sk.getId());
            sm.put("name", sk.getName());
            sm.put("category", sk.getCategory());
            reqSkills.add(sm);
        }
        m.put("requiredSkills", reqSkills);

        m.put("assignments", toAssignmentMaps(assignmentRepository.findByTaskId(task.getId())));
        m.put("resourceBookings", toBookingMaps(task.getId()));
        return m;
    }

    private List<Map<String, Object>> toAssignmentMaps(final List<Assignment> assignments) {
        final List<Map<String, Object>> list = new ArrayList<>();
        for (final Assignment a : safeList(assignments)) {
            if (a == null || a.getId() == null) {
                continue;
            }
            final Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("status", a.getStatus() != null ? a.getStatus().name() : null);
            m.put("user", userRef(a.getUser()));
            m.put("assignedAt", a.getAssignedAt() != null ? a.getAssignedAt().toString() : null);
            m.put("respondedAt", a.getRespondedAt() != null ? a.getRespondedAt().toString() : null);
            if (a.getRejectionReason() != null && !a.getRejectionReason().isBlank()) {
                m.put("rejectionReason", trimForAi(a.getRejectionReason(), 500));
            }
            list.add(m);
        }
        return list;
    }

    private List<Map<String, Object>> toBookingMaps(final Integer taskId) {
        final List<Map<String, Object>> list = new ArrayList<>();
        int page = 1;
        while (true) {
            final PageResult<ResourceBooking> pr = resourceBookingRepository.findByTaskId(taskId, new PageQuery(page, LARGE_PAGE.size()));
            for (final ResourceBooking b : pr.items()) {
                if (b == null || b.getId() == null) {
                    continue;
                }
                list.add(toBookingMap(b));
            }
            if (page >= pr.totalPages() || pr.items().isEmpty()) {
                break;
            }
            page++;
        }
        return list;
    }

    private static Map<String, Object> toBookingMap(final ResourceBooking b) {
        final Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("status", b.getStatus() != null ? b.getStatus().name() : null);
        m.put("from", b.getReservedFrom() != null ? b.getReservedFrom().toString() : null);
        m.put("to", b.getReservedTo() != null ? b.getReservedTo().toString() : null);
        final Resource r = b.getResource();
        if (r != null) {
            m.put("resourceId", r.getId());
            m.put("resourceName", r.getName());
            m.put("resourceType", r.getType() != null ? r.getType().name() : null);
            m.put("resourceOperational", r.isOperational());
            if (r instanceof ExternalResource ext) {
                m.put("externalApiId", ext.getExternalApiId());
            }
        }
        return m;
    }

    private static Map<String, Object> toIncidentMap(final Incident i) {
        final Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("status", i.getStatus() != null ? i.getStatus().name() : null);
        m.put("severity", i.getSeverity() != null ? i.getSeverity().name() : null);
        m.put("description", trimForAi(i.getDescription(), MAX_INCIDENT_DESCRIPTION));
        m.put("createdAt", i.getCreatedAt() != null ? i.getCreatedAt().toString() : null);
        m.put("resolvedAt", i.getResolvedAt() != null ? i.getResolvedAt().toString() : null);
        if (i.getResolutionNotes() != null && !i.getResolutionNotes().isBlank()) {
            m.put("resolutionNotes", trimForAi(i.getResolutionNotes(), MAX_RESOLUTION_NOTES));
        }
        m.put("reporter", userRef(i.getReporter()));
        if (i.getTask() != null && i.getTask().getId() != null) {
            m.put("taskId", i.getTask().getId());
            m.put("taskTitle", i.getTask().getTitle());
        }
        final Resource res = i.getResource();
        if (res != null && res.getId() != null) {
            m.put("resourceId", res.getId());
            m.put("resourceName", res.getName());
        }
        return m;
    }

    private static Map<String, Object> userRef(final User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        final Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", user.getId());
        m.put("roles", roleNames(user));
        return m;
    }

    private static List<String> roleNames(final User user) {
        if (user == null) {
            return List.of();
        }
        final List<String> names = new ArrayList<>();
        for (final Role r : user.getRoles()) {
            if (r != null && r.getName() != null) {
                names.add(r.getName().name());
            }
        }
        return names;
    }

    private static String trimForAi(final String text, final int maxLen) {
        if (text == null) {
            return null;
        }
        String t = text.replace('\r', ' ').replace('\n', ' ').trim();
        if (t.length() <= maxLen) {
            return t;
        }
        return t.substring(0, maxLen) + "…";
    }

    private static <T> List<T> safeList(final List<T> value) {
        return value == null ? List.of() : value;
    }
}
