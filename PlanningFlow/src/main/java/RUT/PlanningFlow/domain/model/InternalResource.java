package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.utils.DomainAssert;

public class InternalResource extends Resource {
    private String inventoryNumber;

    public InternalResource(final Integer id, final String name, final ResourceType type, final String inventoryNumber) {
        super(id, name, type);
        DomainAssert.notBlank(inventoryNumber, "Для внутреннего ресурса обязателен инвентарный номер", "INVENTORY_NUMBER_REQUIRED");
        this.inventoryNumber = inventoryNumber;
    }

    public void updateInventoryNumber(final String newInventoryNumber) {
        DomainAssert.notBlank(newInventoryNumber, "Для внутреннего ресурса обязателен инвентарный номер", "INVENTORY_NUMBER_REQUIRED");
        this.inventoryNumber = newInventoryNumber;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
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
