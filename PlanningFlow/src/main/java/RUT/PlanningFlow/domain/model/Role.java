package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.utils.DomainAssert;

public class Role {
    private final Integer id;
    private final UserRoles name;

    public Role(final Integer id, final UserRoles name) {
        this.id = id;
        DomainAssert.notNull(name, "Роль обязательна", "ROLE_NAME_REQUIRED");
        this.name = name;
    }

    public Integer getId() { return id; }
    public UserRoles getName() { return name; }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Role that = (Role) o;
        return id != null && that.id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
