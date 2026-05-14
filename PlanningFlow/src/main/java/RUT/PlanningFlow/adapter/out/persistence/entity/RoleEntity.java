package RUT.PlanningFlow.adapter.out.persistence.entity;

import RUT.PlanningFlow.domain.enums.UserRoles;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class RoleEntity extends BaseEntity {
    private UserRoles name;

    public RoleEntity(UserRoles name) {
        this.name = name;
    }

    public RoleEntity() {}

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    public UserRoles getName() {
        return name;
    }

    public void setName(UserRoles name) {
        this.name = name;
    }
}