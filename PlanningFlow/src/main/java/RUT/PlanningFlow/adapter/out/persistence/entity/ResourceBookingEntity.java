package RUT.PlanningFlow.adapter.out.persistence.entity;

import RUT.PlanningFlow.domain.enums.BookingStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "resource_bookings")
public class ResourceBookingEntity extends BaseEntity implements Serializable {
    private TaskEntity task;
    private ResourceEntity resource;
    private BookingStatus status;
    private LocalDateTime reservedFrom;
    private LocalDateTime reservedTo;

    public ResourceBookingEntity() {}

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    public TaskEntity getTask() { return task; }
    public void setTask(TaskEntity task) { this.task = task; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    public ResourceEntity getResource() { return resource; }
    public void setResource(ResourceEntity resource) { this.resource = resource; }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    @Column(name = "reserved_from")
    public LocalDateTime getReservedFrom() { return reservedFrom; }
    public void setReservedFrom(LocalDateTime reservedFrom) { this.reservedFrom = reservedFrom; }

    @Column(name = "reserved_to")
    public LocalDateTime getReservedTo() { return reservedTo; }
    public void setReservedTo(LocalDateTime reservedTo) { this.reservedTo = reservedTo; }
}