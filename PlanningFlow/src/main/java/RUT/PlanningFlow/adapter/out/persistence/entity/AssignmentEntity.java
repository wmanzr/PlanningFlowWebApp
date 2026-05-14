package RUT.PlanningFlow.adapter.out.persistence.entity;

import RUT.PlanningFlow.domain.enums.AssignStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
public class AssignmentEntity extends BaseEntity implements Serializable {
    private TaskEntity task;
    private UserEntity user;
    private AssignStatus status;
    private LocalDateTime assignedAt;
    private LocalDateTime respondedAt;
    private String rejectionReason;

    public AssignmentEntity() {}

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    public TaskEntity getTask() { return task; }
    public void setTask(TaskEntity task) { this.task = task; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AssignStatus getStatus() { return status; }
    public void setStatus(AssignStatus status) { this.status = status; }

    @Column(name = "assignedAt")
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    @Column(name = "responded_at")
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}