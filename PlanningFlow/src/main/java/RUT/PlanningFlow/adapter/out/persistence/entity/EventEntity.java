package RUT.PlanningFlow.adapter.out.persistence.entity;

import RUT.PlanningFlow.domain.enums.EventStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
public class EventEntity extends BaseEntity implements Serializable {
    private String title;
    private String description;
    private EventStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double latitude;
    private Double longitude;
    private UserEntity creator;
    private List<UserEntity> coordinators;

    public EventEntity() {
        this.coordinators = new ArrayList<>();
    }

    @Column(nullable = false)
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @Column(columnDefinition = "TEXT")
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    @Column(name = "start_date")
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    @Column(name = "end_date")
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    @Column(name = "latitude")
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    @Column(name = "longitude")
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    public UserEntity getCreator() { return creator; }
    public void setCreator(UserEntity creator) { this.creator = creator; }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "event_coordinators",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    public List<UserEntity> getCoordinators() { return coordinators; }
    public void setCoordinators(List<UserEntity> coordinators) { this.coordinators = coordinators; }
}