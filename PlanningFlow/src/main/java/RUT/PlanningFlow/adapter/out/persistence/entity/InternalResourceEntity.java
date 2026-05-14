package RUT.PlanningFlow.adapter.out.persistence.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("INTERNAL")
public class InternalResourceEntity extends ResourceEntity {
    private String inventoryNumber;

    public String getInventoryNumber() { return inventoryNumber; }
    public void setInventoryNumber(String inventoryNumber) { this.inventoryNumber = inventoryNumber; }
}