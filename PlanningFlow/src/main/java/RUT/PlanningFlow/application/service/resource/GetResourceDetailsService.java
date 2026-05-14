package RUT.PlanningFlow.application.service.resource;

import RUT.PlanningFlow.application.dto.resource.ExternalResourceResponseDto;
import RUT.PlanningFlow.application.dto.resource.InternalResourceResponseDto;
import RUT.PlanningFlow.application.port.in.resource.GetResourceDetailsQuery;
import RUT.PlanningFlow.application.port.out.repository.ExternalResourceRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.InternalResourceRepositoryPort;
import RUT.PlanningFlow.domain.model.ExternalResource;
import RUT.PlanningFlow.domain.model.InternalResource;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GetResourceDetailsService implements GetResourceDetailsQuery {

    private final InternalResourceRepositoryPort internalResourceRepository;
    private final ExternalResourceRepositoryPort externalResourceRepository;

    public GetResourceDetailsService(
            final InternalResourceRepositoryPort internalResourceRepository,
            final ExternalResourceRepositoryPort externalResourceRepository
    ) {
        DomainAssert.notNull(internalResourceRepository, "Репозиторий внутренних ресурсов обязателен", "INTERNAL_RESOURCE_REPOSITORY_REQUIRED");
        DomainAssert.notNull(externalResourceRepository, "Репозиторий внешних ресурсов обязателен", "EXTERNAL_RESOURCE_REPOSITORY_REQUIRED");
        this.internalResourceRepository = internalResourceRepository;
        this.externalResourceRepository = externalResourceRepository;
    }

    @Override
    public Optional<InternalResourceResponseDto> execute(final Integer resourceId) {
        DomainAssert.notNull(resourceId, "ID ресурса обязателен", "RESOURCE_ID_REQUIRED");
        final Optional<InternalResource> exResource = internalResourceRepository.findById(resourceId);
        return exResource.map(InternalResourceResponseDto::from);
    }

    @Override
    public Optional<ExternalResourceResponseDto> executeExternal(final Integer resourceId) {
        DomainAssert.notNull(resourceId, "ID ресурса обязателен", "RESOURCE_ID_REQUIRED");
        final Optional<ExternalResource> exResource = externalResourceRepository.findById(resourceId);
        return exResource.map(ExternalResourceResponseDto::from);
    }
}