package RUT.PlanningFlow.adapter.out.persistence.entity;

import RUT.PlanningFlow.domain.enums.ResourceType;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "resources")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "source_type", discriminatorType = DiscriminatorType.STRING)
public abstract class ResourceEntity extends BaseEntity implements Serializable {
    private String name;
    private ResourceType type;
    private boolean operational = true;

    @Column(nullable = false)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ResourceType getType() { return type; }
    public void setType(ResourceType type) { this.type = type; }

    @Column(name = "operational", nullable = false)
    public boolean isOperational() { return operational; }
    public void setOperational(boolean operational) { this.operational = operational; }
}