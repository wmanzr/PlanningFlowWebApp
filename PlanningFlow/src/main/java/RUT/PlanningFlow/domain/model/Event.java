package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.vo.DateTimeRange;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import RUT.PlanningFlow.domain.utils.DomainAssert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Event {
    private final Integer id;
    private String title;
    private String description;
    private EventStatus status;
    private DateTimeRange schedule;
    private GeoPoint location;
    private final User creator;
    private final List<User> coordinators;

    public Event(
            final Integer id,
            final String title,
            final String description,
            final EventStatus status,
            final LocalDateTime startDate,
            final LocalDateTime endDate,
            final GeoPoint location,
            final User creator,
            final List<User> coordinators
    ) {
        this.id = id;
        DomainAssert.notBlank(title, "Название мероприятия обязательно", "EVENT_TITLE_REQUIRED");
        this.title = title;
        this.description = description;
        DomainAssert.notNull(status, "Статус мероприятия обязателен", "EVENT_STATUS_REQUIRED");
        this.status = status;
        this.schedule = new DateTimeRange(startDate, endDate);
        this.location = location;
        DomainAssert.notNull(creator, "Создатель мероприятия обязателен", "EVENT_CREATOR_REQUIRED");
        this.creator = creator;
        this.coordinators = coordinators == null ? new ArrayList<>() : new ArrayList<>(coordinators);
    }

    public void startPlanning() {
        if (this.status == EventStatus.PLANNING) {
            return;
        }
        if (this.status != EventStatus.DRAFT) {
            throw new DomainException("Планирование можно начать только для черновика", "INVALID_EVENT_STATE");
        }
        if (this.coordinators == null || this.coordinators.isEmpty()) {
            throw new DomainException("Нельзя начать планирование без выбранного координатора", "EVENT_COORDINATOR_REQUIRED");
        }
        this.status = EventStatus.PLANNING;
    }

    public void activate() {
        if (this.status == EventStatus.ACTIVE) {
            return;
        }
        if (this.status != EventStatus.PLANNING) {
            throw new DomainException("Активировать можно только мероприятие в планировании", "INVALID_EVENT_STATE");
        }
        this.status = EventStatus.ACTIVE;
    }

    public void validateTaskAttach(final Task task) {
        DomainAssert.notNull(task, "Задача обязательна", "TASK_REQUIRED");
        DomainAssert.isTrue(task.getEvent() == this, "Задача должна относиться к этому мероприятию", "TASK_WRONG_EVENT");
        if (task.getStartTime().isBefore(this.schedule.getStart()) || task.getEndTime().isAfter(this.schedule.getEnd())) {
            throw new DomainException("Время задачи выходит за временные рамки мероприятия", "TASK_OUT_OF_EVENT_RANGE");
        }
    }

    public double calculateProgress(final List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return 0.0;
        }

        long completedCount = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();

        return (double) completedCount / tasks.size() * 100;
    }

    public void complete(final List<Task> tasks) {
        complete(tasks, List.of());
    }

    
    public void complete(final List<Task> tasks, final List<Incident> incidentsToAutoResolve) {
        if (this.status == EventStatus.COMPLETED) {
            return;
        }
        final List<Task> safeTasks = tasks == null ? List.of() : tasks;
        final boolean hasBlockingTasks = safeTasks.stream().anyMatch(t -> t != null && !isTaskTerminatedForEventCompletion(t));

        if (hasBlockingTasks) {
            throw new DomainException(
                    "Нельзя завершить мероприятие, пока все задачи не завершены или не отменены",
                    "INCOMPLETE_TASKS_FOR_EVENT_COMPLETION"
            );
        }

        this.status = EventStatus.COMPLETED;
        final String autoNote = "Мероприятие завершено; инцидент закрыт автоматически.";
        if (incidentsToAutoResolve != null) {
            for (final Incident incident : incidentsToAutoResolve) {
                if (incident != null && incident.getEvent() != null && this.id != null
                        && this.id.equals(incident.getEvent().getId())
                        && (incident.getStatus() == IncidentStatus.OPEN || incident.getStatus() == IncidentStatus.IN_PROGRESS)) {
                    incident.resolve(autoNote);
                }
            }
        }
    }

    private static boolean isTaskTerminatedForEventCompletion(final Task task) {
        final TaskStatus status = task.getStatus();
        return status == TaskStatus.DONE || status == TaskStatus.CANCELLED;
    }

    public void cancel(final String reason) {
        cancel(reason, List.of());
    }

    public void cancel(final String reason, final List<Incident> incidentsToAutoResolve) {
        if (this.status == EventStatus.CANCELLED) {
            return;
        }
        if (this.status == EventStatus.COMPLETED) {
            throw new DomainException("Нельзя отменить завершенное мероприятие", "INVALID_EVENT_STATE");
        }
        if (reason == null || reason.isBlank()) {
            throw new DomainException("Причина отмены обязательна", "CANCEL_REASON_REQUIRED");
        }
        this.status = EventStatus.CANCELLED;
        final String autoNote = "Мероприятие отменено; инцидент закрыт автоматически.";
        if (incidentsToAutoResolve != null) {
            for (final Incident incident : incidentsToAutoResolve) {
                if (incident != null && incident.getEvent() != null && this.id != null
                        && this.id.equals(incident.getEvent().getId())
                        && (incident.getStatus() == IncidentStatus.OPEN || incident.getStatus() == IncidentStatus.IN_PROGRESS)) {
                    incident.resolve(autoNote);
                }
            }
        }
    }

    public void assertAllowsPlannerContentMutations() {
        if (this.status == EventStatus.COMPLETED || this.status == EventStatus.CANCELLED) {
            throw new DomainException(
                    "Мероприятие завершено или отменено: изменения недоступны",
                    "EVENT_CLOSED"
            );
        }
    }

    public void assertAllowsReportingIncidents() {
        if (this.status == EventStatus.COMPLETED || this.status == EventStatus.CANCELLED) {
            throw new DomainException(
                    "Нельзя регистрировать инциденты для завершенного или отмененного мероприятия",
                    "EVENT_CLOSED_FOR_INCIDENTS"
            );
        }
    }

    public void updateInfo(final String newTitle, final String newDescription) {
        assertAllowsPlannerContentMutations();
        DomainAssert.notBlank(newTitle, "Название мероприятия обязательно", "EVENT_TITLE_REQUIRED");
        this.title = newTitle;
        this.description = newDescription;
    }

    public void updateLocation(final GeoPoint newLocation) {
        assertAllowsPlannerContentMutations();
        this.location = newLocation;
    }

    public void clearLocation() {
        assertAllowsPlannerContentMutations();
        this.location = null;
    }

    public void addCoordinator(final User coordinator) {
        assertAllowsPlannerContentMutations();
        DomainAssert.notNull(coordinator, "Координатор обязателен", "EVENT_COORDINATOR_REQUIRED");
        DomainAssert.notNull(coordinator.getId(), "Координатор должен иметь id", "EVENT_COORDINATOR_ID_REQUIRED");
        final boolean exists = coordinators.stream().anyMatch(u -> u != null && coordinator.getId().equals(u.getId()));
        if (exists) {
            return;
        }
        coordinators.add(coordinator);
    }

    public void removeCoordinator(final User coordinator) {
        assertAllowsPlannerContentMutations();
        DomainAssert.notNull(coordinator, "Координатор обязателен", "EVENT_COORDINATOR_REQUIRED");
        DomainAssert.notNull(coordinator.getId(), "Координатор должен иметь id", "EVENT_COORDINATOR_ID_REQUIRED");
        coordinators.removeIf(u -> u != null && coordinator.getId().equals(u.getId()));
    }

    public void clearCoordinators() {
        assertAllowsPlannerContentMutations();
        coordinators.clear();
    }

    public void updateDates(final LocalDateTime newStartDate, final LocalDateTime newEndDate, final List<Task> tasks) {
        if (this.status == EventStatus.COMPLETED || this.status == EventStatus.CANCELLED) {
            throw new DomainException("Нельзя менять даты завершенного или отмененного мероприятия", "INVALID_EVENT_STATE");
        }
        DomainAssert.notNull(newStartDate, "Дата начала обязательна при изменении дат", "EVENT_START_DATE_REQUIRED");
        DomainAssert.notNull(newEndDate, "Дата окончания обязательна при изменении дат", "EVENT_END_DATE_REQUIRED");
        final DateTimeRange newSchedule = new DateTimeRange(newStartDate, newEndDate);
        DomainAssert.notNull(tasks, "Список задач обязателен для изменения дат мероприятия", "EVENT_TASKS_REQUIRED");
        for (final Task task : tasks) {
            if (task.getStartTime().isBefore(newStartDate) || task.getEndTime().isAfter(newEndDate)) {
                throw new DomainException(
                        "Нельзя изменить даты мероприятия: задача выходит за новый диапазон",
                        "TASK_OUT_OF_EVENT_RANGE"
                );
            }
        }
        this.schedule = newSchedule;
    }

    public void reschedule(final LocalDateTime newStartDate, final LocalDateTime newEndDate, final List<Task> tasks) {
        updateDates(newStartDate, newEndDate, tasks);
    }

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public EventStatus getStatus() { return status; }
    public LocalDateTime getStartDate() { return schedule.getStart(); }
    public LocalDateTime getEndDate() { return schedule.getEnd(); }
    public DateTimeRange getSchedule() { return schedule; }
    public GeoPoint getLocation() { return location; }
    public Double getLatitude() { return location == null ? null : location.getLatitude(); }
    public Double getLongitude() { return location == null ? null : location.getLongitude(); }
    public User getCreator() { return creator; }
    public List<User> getCoordinators() { return Collections.unmodifiableList(coordinators); }
    public User getEffectiveCoordinator() { return coordinators.isEmpty() ? creator : coordinators.get(0); }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Event that = (Event) o;
        return id != null && that.id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}