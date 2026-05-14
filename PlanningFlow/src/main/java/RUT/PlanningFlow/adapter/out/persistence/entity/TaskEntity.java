package RUT.PlanningFlow.adapter.out.persistence.entity;

import RUT.PlanningFlow.domain.enums.TaskStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
public class TaskEntity extends BaseEntity implements Serializable {
    private EventEntity event;
    private UserEntity createdBy;
    private String title;
    private TaskStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double latitude;
    private Double longitude;
    private List<SkillEntity> requiredSkills;
    private List<TaskEntity> dependencies;

    public TaskEntity() {
        this.requiredSkills = new ArrayList<>();
        this.dependencies = new ArrayList<>();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    public EventEntity getEvent() { return event; }
    public void setEvent(EventEntity event) { this.event = event; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    public UserEntity getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserEntity createdBy) { this.createdBy = createdBy; }

    @Column(nullable = false)
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    @Column(name = "start_time")
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    @Column(name = "end_time")
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    @Column(name = "latitude")
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    @Column(name = "longitude")
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    @ManyToMany
    @JoinTable(
            name = "task_required_skills",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    public List<SkillEntity> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<SkillEntity> requiredSkills) { this.requiredSkills = requiredSkills; }

    
    @ManyToMany
    @JoinTable(
            name = "task_dependencies",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "depends_on_task_id")
    )
    public List<TaskEntity> getDependencies() { return dependencies; }
    public void setDependencies(List<TaskEntity> dependencies) { this.dependencies = dependencies; }
}