package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.utils.DomainAssert;

public class ExternalResource extends Resource {
    private String externalApiId;

    public ExternalResource(final Integer id, final String name, final ResourceType type, final String externalApiId) {
        super(id, name, type);
        DomainAssert.notBlank(externalApiId, "Для внешнего ресурса обязателен идентификатор API поставщика", "EXTERNAL_API_ID_REQUIRED");
        this.externalApiId = externalApiId;
    }

    public void updateExternalApiId(final String newExternalApiId) {
        DomainAssert.notBlank(newExternalApiId, "Для внешнего ресурса обязателен идентификатор API поставщика", "EXTERNAL_API_ID_REQUIRED");
        this.externalApiId = newExternalApiId;
    }

    public String getExternalApiId() {
        return externalApiId;
    }

    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
