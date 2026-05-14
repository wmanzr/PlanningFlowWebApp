package RUT.PlanningFlow.application.port.in.resource;

import RUT.PlanningFlow.application.dto.resource.ExternalResourceResponseDto;
import RUT.PlanningFlow.application.dto.resource.InternalResourceResponseDto;

import java.util.Optional;

public interface GetResourceDetailsQuery {
    Optional<InternalResourceResponseDto> execute(Integer resourceId);
    Optional<ExternalResourceResponseDto> executeExternal(Integer resourceId);
}