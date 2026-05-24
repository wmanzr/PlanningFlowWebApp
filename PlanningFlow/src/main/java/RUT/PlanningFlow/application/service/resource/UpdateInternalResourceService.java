package RUT.PlanningFlow.application.service.resource;

import RUT.PlanningFlow.application.port.in.resource.UpdateInternalResourceUseCase;
import RUT.PlanningFlow.application.port.out.repository.InternalResourceRepositoryPort;
import RUT.PlanningFlow.config.cache.ApplicationRedisCacheConfiguration;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.model.InternalResource;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UpdateInternalResourceService implements UpdateInternalResourceUseCase {

    private final InternalResourceRepositoryPort internalResourceRepository;

    public UpdateInternalResourceService(final InternalResourceRepositoryPort internalResourceRepository) {
        DomainAssert.notNull(internalResourceRepository, "Репозиторий внутренних ресурсов обязателен", "INTERNAL_RESOURCE_REPOSITORY_REQUIRED");
        this.internalResourceRepository = internalResourceRepository;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = ApplicationRedisCacheConfiguration.CACHE_INTERNAL_RESOURCES, allEntries = true),
            @CacheEvict(cacheNames = ApplicationRedisCacheConfiguration.CACHE_RESOURCE_DETAILS, key = "#resourceId")
    })
    public Optional<Integer> execute(
            final Integer resourceId,
            final String name,
            final ResourceType type,
            final String inventoryNumber
    ) {
        DomainAssert.notNull(resourceId, "ID ресурса обязателен", "RESOURCE_ID_REQUIRED");

        final Optional<InternalResource> exResource = internalResourceRepository.findById(resourceId);
        if (exResource.isEmpty()) {
            return Optional.empty();
        }

        final InternalResource resource = exResource.get();
        boolean touched = false;
        if (name != null) {
            resource.rename(name);
            touched = true;
        }
        if (type != null) {
            resource.changeType(type);
            touched = true;
        }
        if (inventoryNumber != null) {
            resource.updateInventoryNumber(inventoryNumber);
            touched = true;
        }

        if (!touched) {
            return Optional.of(resourceId);
        }

        return internalResourceRepository.update(resource);
    }
}