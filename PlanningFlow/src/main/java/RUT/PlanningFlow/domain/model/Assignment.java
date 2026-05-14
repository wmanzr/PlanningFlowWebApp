package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.utils.DomainAssert;

import java.time.LocalDateTime;

public class Assignment {
    private final Integer id;
    private final Task task;
    private final User user;
    private AssignStatus status;
    private LocalDateTime assignedAt;
    private LocalDateTime respondedAt;
    private String rejectionReason;

    public Assignment(
            final Integer id,
            final Task task,
            final User user,
            final AssignStatus status,
            final LocalDateTime assignedAt,
            final LocalDateTime respondedAt,
            final String rejectionReason
    ) {
        this.id = id;
        DomainAssert.notNull(task, "Задача назначения обязательна", "TASK_REQUIRED");
        DomainAssert.notNull(user, "Исполнитель назначения обязателен", "USER_REQUIRED");
        DomainAssert.notNull(status, "Статус назначения обязателен", "ASSIGNMENT_STATUS_REQUIRED");
        DomainAssert.notNull(assignedAt, "Время назначения обязательно", "ASSIGNED_AT_REQUIRED");
        this.task = task;
        this.user = user;
        this.status = status;
        this.assignedAt = assignedAt;
        this.respondedAt = respondedAt;
        this.rejectionReason = rejectionReason;
    }

    public void accept(final LocalDateTime responseTime) {
        if (status == AssignStatus.ACCEPTED) {
            return;
        }
        if (status != AssignStatus.PENDING) {
            throw new DomainException("Подтвердить можно только назначение в статусе PENDING", "INVALID_ASSIGNMENT_STATE");
        }
        DomainAssert.notNull(responseTime, "Время ответа обязательно", "RESPONSE_TIME_REQUIRED");
        this.status = AssignStatus.ACCEPTED;
        this.respondedAt = responseTime;
        this.rejectionReason = null;
    }

    public void reject(final LocalDateTime responseTime, final String reason) {
        if (status == AssignStatus.REJECTED) {
            return;
        }
        if (status != AssignStatus.PENDING && status != AssignStatus.ACCEPTED) {
            throw new DomainException("Отказаться можно только от активного назначения", "INVALID_ASSIGNMENT_STATE");
        }
        DomainAssert.notNull(responseTime, "Время ответа обязательно", "RESPONSE_TIME_REQUIRED");
        if (reason == null || reason.isBlank()) {
            throw new DomainException("Причина отказа обязательна", "REJECTION_REASON_REQUIRED");
        }
        this.status = AssignStatus.REJECTED;
        this.rejectionReason = reason;
        this.respondedAt = responseTime;
    }

    public void cancelByCoordinator(final LocalDateTime responseTime) {
        if (status == AssignStatus.CANCELLED) {
            return;
        }
        if (status == AssignStatus.REJECTED) {
            throw new DomainException("Нельзя отменить неактивное назначение", "INVALID_ASSIGNMENT_STATE");
        }
        DomainAssert.notNull(responseTime, "Время ответа обязательно", "RESPONSE_TIME_REQUIRED");
        this.status = AssignStatus.CANCELLED;
        this.respondedAt = responseTime;
        this.rejectionReason = null;
    }

    public Integer getId() { return id; }
    public Task getTask() { return task; }
    public User getUser() { return user; }
    public AssignStatus getStatus() { return status; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public String getRejectionReason() { return rejectionReason; }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Assignment that = (Assignment) o;
        return id != null && that.id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}