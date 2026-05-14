package RUT.PlanningFlow.adapter.in.web.dto.resource;

import RUT.PlanningFlow.domain.enums.ResourceType;
import jakarta.validation.constraints.Size;

public class InternalResourceUpdateRequest {

    @Size(max = 2048, message = "Название слишком длинное")
    private String name;

    private ResourceType type;

    @Size(max = 512, message = "Инвентарный номер слишком длинный")
    private String inventoryNumber;

    public InternalResourceUpdateRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(final ResourceType type) {
        this.type = type;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
    }

    public void setInventoryNumber(final String inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }
}
