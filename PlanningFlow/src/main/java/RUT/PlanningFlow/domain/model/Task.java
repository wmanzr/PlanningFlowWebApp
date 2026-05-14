package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.vo.DateTimeRange;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import RUT.PlanningFlow.domain.utils.DomainAssert;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Task {

    private static final Duration MAX_TASK_DURATION = Duration.ofHours(8);

    private final Integer id;
    private Event event;
    private final User createdBy;
    private String title;
    private TaskStatus status;
    private DateTimeRange schedule;
    private GeoPoint location;
    private final List<Skill> requiredSkills;
    private final List<Task> dependencies;

    public Task(
            final Integer id,
            final Event event,
            final User createdBy,
            final String title,
            final TaskStatus status,
            final LocalDateTime startTime,
            final LocalDateTime endTime,
            final GeoPoint location,
            final List<Skill> requiredSkills,
            final List<Task> dependencies
    ) {
        this.id = id;
        DomainAssert.notNull(event, "Мероприятие задачи обязательно", "EVENT_REQUIRED");
        this.event = event;
        DomainAssert.notNull(createdBy, "Создатель задачи обязателен", "TASK_CREATOR_REQUIRED");
        DomainAssert.notNull(createdBy.getId(), "Создатель задачи должен иметь id", "TASK_CREATOR_ID_REQUIRED");
        this.createdBy = createdBy;
        DomainAssert.notBlank(title, "Название задачи обязательно", "TASK_TITLE_REQUIRED");
        this.title = title;
        DomainAssert.notNull(status, "Статус задачи обязателен", "TASK_STATUS_REQUIRED");
        this.status = status;
        
        this.schedule = new DateTimeRange(startTime, endTime);
        this.location = location;
        this.requiredSkills = requiredSkills == null ? new ArrayList<>() : new ArrayList<>(requiredSkills);
        this.dependencies = dependencies == null ? new ArrayList<>() : new ArrayList<>(dependencies);
    }

    public void rename(final String newTitle) {
        DomainAssert.notBlank(newTitle, "Название задачи обязательно", "TASK_TITLE_REQUIRED");
        this.title = newTitle;
    }

    public void moveSchedule(final LocalDateTime newStartTime, final LocalDateTime newEndTime) {
        if (event != null) {
            if (newStartTime.isBefore(event.getStartDate()) || newEndTime.isAfter(event.getEndDate())) {
                throw new DomainException("Время задачи выходит за временные рамки мероприятия", "TASK_OUT_OF_EVENT_RANGE");
            }
        }
        assertScheduleDurationAllowed(newStartTime, newEndTime);
        this.schedule = new DateTimeRange(newStartTime, newEndTime);
    }

    
    public static void assertScheduleDurationAllowed(final LocalDateTime startTime, final LocalDateTime endTime) {
        final DateTimeRange schedule = new DateTimeRange(startTime, endTime);
        if (schedule.duration().compareTo(MAX_TASK_DURATION) > 0) {
            throw new DomainException("Задача не может длиться больше 8 часов", "TASK_DURATION_EXCEEDS_LIMIT");
        }
    }

    public void updateLocation(final GeoPoint newLocation) {
        this.location = newLocation;
    }

    public void clearLocation() {
        this.location = null;
    }

    public void addRequiredSkill(final Skill skill) {
        DomainAssert.notNull(skill, "Требуемый навык обязателен", "REQUIRED_SKILL_REQUIRED");
        DomainAssert.notNull(skill.getId(), "Требуемый навык должен иметь id", "REQUIRED_SKILL_ID_REQUIRED");
        final boolean exists = requiredSkills.stream().anyMatch(item -> Objects.equals(item.getId(), skill.getId()));
        if (exists) {
            return;
        }
        requiredSkills.add(skill);
    }

    public void removeRequiredSkill(final Skill skill) {
        DomainAssert.notNull(skill, "Требуемый навык обязателен", "REQUIRED_SKILL_REQUIRED");
        DomainAssert.notNull(skill.getId(), "Требуемый навык должен иметь id", "REQUIRED_SKILL_ID_REQUIRED");
        requiredSkills.removeIf(item -> Objects.equals(item.getId(), skill.getId()));
    }

    public void addDependency(final Task dependency) {
        DomainAssert.notNull(dependency, "Зависимость задачи обязательна", "DEPENDENCY_REQUIRED");
        DomainAssert.notNull(dependency.getId(), "Зависимость должна иметь id", "DEPENDENCY_ID_REQUIRED");
        if (this.event != null && dependency.getEvent() != null && !this.event.equals(dependency.getEvent())) {
            throw new DomainException("Зависимость должна относиться к тому же мероприятию", "DEPENDENCY_WRONG_EVENT");
        }
        if (java.util.Objects.equals(this.id, dependency.getId())) {
            throw new DomainException("Задача не может зависеть сама от себя", "SELF_DEPENDENCY_NOT_ALLOWED");
        }
        if (dependsOn(dependency, this)) {
            throw new DomainException("Нельзя создать циклическую зависимость задач", "CYCLIC_DEPENDENCY_NOT_ALLOWED");
        }

        final LocalDateTime start = this.getStartTime();
        final LocalDateTime dependencyEnd = dependency.getEndTime();
        if (start != null && dependencyEnd != null && start.isBefore(dependencyEnd)) {
            throw new DomainException("Задача не может начаться раньше завершения зависимости", "DEPENDENCY_SCHEDULE_CONFLICT");
        }
        final boolean exists = dependencies.stream().anyMatch(item -> Objects.equals(item.getId(), dependency.getId()));
        if (exists) {
            return;
        }
        dependencies.add(dependency);
    }

    public void removeDependency(final Task dependency) {
        DomainAssert.notNull(dependency, "Зависимость задачи обязательна", "DEPENDENCY_REQUIRED");
        DomainAssert.notNull(dependency.getId(), "Зависимость должна иметь id", "DEPENDENCY_ID_REQUIRED");
        dependencies.removeIf(item -> item != null && Objects.equals(item.getId(), dependency.getId()));
    }

    public void assign(final Assignment assignment) {
        DomainAssert.notNull(assignment, "Назначение обязательно", "ASSIGNMENT_REQUIRED");
        DomainAssert.isTrue(
                assignmentLinksThisTask(assignment),
                "Назначение должно ссылаться на эту задачу",
                "ASSIGNMENT_TASK_MISMATCH"
        );
        if (status == TaskStatus.DONE || status == TaskStatus.CANCELLED) {
            throw new DomainException(
                    "Нельзя подтвердить участие в завершенной или отмененной задаче",
                    "INVALID_TASK_STATE"
            );
        }
        if (status == TaskStatus.ASSIGNED || status == TaskStatus.IN_PROGRESS) {
            return;
        }
        DomainAssert.isTrue(status == TaskStatus.OPEN, "Назначить можно только задачу в статусе OPEN", "INVALID_TASK_STATE");
        this.status = TaskStatus.ASSIGNED;
    }

    public void unassign(final Assignment assignment) {
        DomainAssert.notNull(assignment, "Назначение обязательно", "ASSIGNMENT_REQUIRED");
        DomainAssert.isTrue(
                assignmentLinksThisTask(assignment),
                "Назначение должно ссылаться на эту задачу",
                "ASSIGNMENT_TASK_MISMATCH"
        );
        if (status == TaskStatus.OPEN) {
            return;
        }
        DomainAssert.isTrue(status == TaskStatus.ASSIGNED, "Снять назначение можно только со статуса ASSIGNED", "INVALID_TASK_STATE");
        this.status = TaskStatus.OPEN;
    }

    public void startExecution() {
        if (status == TaskStatus.IN_PROGRESS) {
            return;
        }
        if (status != TaskStatus.ASSIGNED) {
            throw new DomainException("Задачу можно начать только после назначения", "INVALID_TASK_STATE");
        }
        ensureDependenciesDone();
        this.status = TaskStatus.IN_PROGRESS;
    }

    private void ensureDependenciesDone() {
        for (final Task dependency : dependencies) {
            if (dependency == null) {
                continue;
            }
            if (dependency.getStatus() != TaskStatus.DONE) {
                throw new DomainException(
                        "Нельзя начать задачу, пока не выполнены все родительские задачи",
                        "DEPENDENCIES_NOT_DONE"
                );
            }
        }
    }

    public void assertEligibleForNewIncidentAttachment() {
        if (status == TaskStatus.DONE || status == TaskStatus.CANCELLED) {
            throw new DomainException(
                    "Нельзя создать инцидент для завершенной или отмененной задачи",
                    "TASK_CLOSED_FOR_INCIDENT"
            );
        }
    }

    public void markAsDone() {
        if (status == TaskStatus.DONE) {
            return;
        }
        if (status != TaskStatus.IN_PROGRESS) {
            throw new DomainException("Задача должна быть в работе для завершения", "INVALID_TASK_STATE");
        }
        this.status = TaskStatus.DONE;
    }

    public void cancel() {
        if (status == TaskStatus.CANCELLED) {
            return;
        }
        if (status == TaskStatus.DONE) {
            throw new DomainException("Нельзя отменить выполненную задачу", "INVALID_TASK_STATE");
        }
        this.status = TaskStatus.CANCELLED;
    }

    
    private boolean assignmentLinksThisTask(final Assignment assignment) {
        final Task at = assignment.getTask();
        return at != null && this.id != null && Objects.equals(at.getId(), this.id);
    }

    private static boolean dependsOn(final Task root, final Task target) {
        if (root == null || target == null) {
            return false;
        }
        final List<Task> deps = root.getDependencies();
        for (final Task d : deps) {
            if (d == null) {
                continue;
            }
            if (d == target) {
                return true;
            }
            if (dependsOn(d, target)) {
                return true;
            }
        }
        return false;
    }

    public Integer getId() { return id; }
    public Event getEvent() { return event; }
    public User getCreatedBy() { return createdBy; }
    public String getTitle() { return title; }
    public TaskStatus getStatus() { return status; }
    public LocalDateTime getStartTime() { return schedule.getStart(); }
    public LocalDateTime getEndTime() { return schedule.getEnd(); }
    public DateTimeRange getSchedule() { return schedule; }
    public GeoPoint getLocation() { return location; }
    public Double getLatitude() { return location == null ? null : location.getLatitude(); }
    public Double getLongitude() { return location == null ? null : location.getLongitude(); }
    public List<Skill> getRequiredSkills() { return Collections.unmodifiableList(requiredSkills); }
    public List<Task> getDependencies() { return Collections.unmodifiableList(dependencies); }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Task that = (Task) o;
        return id != null && that.id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}