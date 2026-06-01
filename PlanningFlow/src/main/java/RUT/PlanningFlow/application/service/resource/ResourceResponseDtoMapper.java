package RUT.PlanningFlow.application.service.resource;

import RUT.PlanningFlow.application.dto.resource.ExternalResourceResponseDto;
import RUT.PlanningFlow.application.dto.resource.InternalResourceResponseDto;
import RUT.PlanningFlow.domain.model.ExternalResource;
import RUT.PlanningFlow.domain.model.InternalResource;
import org.springframework.stereotype.Component;

@Component
public final class ResourceResponseDtoMapper {

    public InternalResourceResponseDto toInternal(final InternalResource resource) {
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

    public ExternalResourceResponseDto toExternal(final ExternalResource resource) {
        if (resource == null) {
            return null;
        }
        return new ExternalResourceResponseDto(
                resource.getId(),
                resource.getName(),
                resource.getType(),
                resource.getExternalApiId(),
                resource.isOperational()
        );
    }
}
