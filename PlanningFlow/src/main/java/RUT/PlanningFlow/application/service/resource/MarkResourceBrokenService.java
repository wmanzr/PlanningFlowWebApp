package RUT.PlanningFlow.application.service.resource;

import RUT.PlanningFlow.application.port.in.resource.MarkResourceBrokenUseCase;
import RUT.PlanningFlow.application.port.out.repository.InternalResourceRepositoryPort;
import RUT.PlanningFlow.config.cache.ApplicationRedisCacheConfiguration;
import RUT.PlanningFlow.domain.model.InternalResource;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class MarkResourceBrokenService implements MarkResourceBrokenUseCase {

    private final InternalResourceRepositoryPort internalResourceRepository;

    public MarkResourceBrokenService(final InternalResourceRepositoryPort internalResourceRepository) {
        DomainAssert.notNull(internalResourceRepository, "Репозиторий внутренних ресурсов обязателен", "INTERNAL_RESOURCE_REPOSITORY_REQUIRED");
        this.internalResourceRepository = internalResourceRepository;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = ApplicationRedisCacheConfiguration.CACHE_INTERNAL_RESOURCES, allEntries = true),
            @CacheEvict(cacheNames = ApplicationRedisCacheConfiguration.CACHE_RESOURCE_DETAILS, key = "#resourceId")
    })
    public Optional<Integer> execute(final Integer resourceId) {
        DomainAssert.notNull(resourceId, "ID ресурса обязателен", "RESOURCE_ID_REQUIRED");
        final Optional<InternalResource> exResource = internalResourceRepository.findById(resourceId);
        if (exResource.isEmpty()) {
            return Optional.empty();
        }

        final InternalResource resource = exResource.get();
        resource.markBroken();
        return internalResourceRepository.update(resource);
    }
}