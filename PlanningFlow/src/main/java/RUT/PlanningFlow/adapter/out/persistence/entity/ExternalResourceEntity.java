package RUT.PlanningFlow.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("EXTERNAL")
public class ExternalResourceEntity extends ResourceEntity {
    private String externalApiId;

    @Column(name = "external_api_id")
    public String getExternalApiId() { return externalApiId; }
    public void setExternalApiId(String externalApiId) { this.externalApiId = externalApiId; }
}