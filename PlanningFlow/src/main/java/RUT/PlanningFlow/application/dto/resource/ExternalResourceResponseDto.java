package RUT.PlanningFlow.application.dto.resource;

import RUT.PlanningFlow.domain.enums.ResourceType;

public final class ExternalResourceResponseDto {
    private final Integer id;
    private final String name;
    private final ResourceType type;
    private final String externalApiId;
    private final boolean operational;

    public ExternalResourceResponseDto(
            final Integer id,
            final String name,
            final ResourceType type,
            final String externalApiId,
            final boolean operational
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.externalApiId = externalApiId;
        this.operational = operational;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public ResourceType getType() { return type; }
    public String getExternalApiId() { return externalApiId; }
    public boolean isOperational() { return operational; }
}
