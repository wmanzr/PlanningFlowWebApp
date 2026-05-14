package RUT.PlanningFlow.adapter.in.web.dto.resource;

import RUT.PlanningFlow.domain.enums.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InternalResourceCreateRequest {

    @NotBlank(message = "Название ресурса обязательно")
    @Size(max = 2048, message = "Название слишком длинное")
    private String name;

    @NotNull(message = "Тип ресурса обязателен")
    private ResourceType type;

    @NotBlank(message = "Инвентарный номер обязателен")
    @Size(max = 512, message = "Инвентарный номер слишком длинный")
    private String inventoryNumber;

    public InternalResourceCreateRequest() {
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
