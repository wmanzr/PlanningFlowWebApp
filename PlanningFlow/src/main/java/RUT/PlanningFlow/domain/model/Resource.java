package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.utils.DomainAssert;

public abstract class Resource {
    private final Integer id;
    private String name;
    private ResourceType type;
    private boolean operational;

    protected Resource(final Integer id, final String name, final ResourceType type) {
        this.id = id;
        DomainAssert.notBlank(name, "Название ресурса не может быть пустым", "RESOURCE_NAME_REQUIRED");
        this.name = name;
        DomainAssert.notNull(type, "Тип ресурса обязателен", "RESOURCE_TYPE_REQUIRED");
        this.type = type;
        this.operational = true;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public ResourceType getType() { return type; }
    public boolean isOperational() { return operational; }

    public void rename(final String newName) {
        DomainAssert.notBlank(newName, "Название ресурса не может быть пустым", "RESOURCE_NAME_REQUIRED");
        this.name = newName;
    }

    public void changeType(final ResourceType newType) {
        DomainAssert.notNull(newType, "Тип ресурса обязателен", "RESOURCE_TYPE_REQUIRED");
        this.type = newType;
    }

    public void markBroken() {
        if (!this.operational) {
            return;
        }
        this.operational = false;
    }

    public void markOperational() {
        if (this.operational) {
            return;
        }
        this.operational = true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Resource that = (Resource) o;
        return id != null && that.id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}