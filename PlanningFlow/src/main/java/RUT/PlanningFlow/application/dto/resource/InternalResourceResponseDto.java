package RUT.PlanningFlow.application.dto.resource;

import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.model.InternalResource;

public final class InternalResourceResponseDto {
    private final Integer id;
    private final String name;
    private final ResourceType type;
    private final String inventoryNumber;
    private final boolean operational;

    public InternalResourceResponseDto(
            final Integer id,
            final String name,
            final ResourceType type,
            final String inventoryNumber,
            final boolean operational
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.inventoryNumber = inventoryNumber;
        this.operational = operational;
    }

    public static InternalResourceResponseDto from(final InternalResource resource) {
        if (resource == null) {
            return null;
        }
        return new InternalResourceResponseDto(
                resource.getId(),
                resource.getName(),
                resource.getType(),
                resource.getInventoryNumber(),
                resource.isOperational()
        );
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public ResourceType getType() { return type; }
    public String getInventoryNumber() { return inventoryNumber; }
    public boolean isOperational() { return operational; }
}