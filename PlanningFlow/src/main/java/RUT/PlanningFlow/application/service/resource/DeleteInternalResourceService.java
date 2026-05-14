package RUT.PlanningFlow.application.service.resource;

import RUT.PlanningFlow.application.port.in.resource.DeleteInternalResourceUseCase;
import RUT.PlanningFlow.application.port.out.repository.InternalResourceRepositoryPort;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteInternalResourceService implements DeleteInternalResourceUseCase {

    private final InternalResourceRepositoryPort internalResourceRepository;

    public DeleteInternalResourceService(final InternalResourceRepositoryPort internalResourceRepository) {
        DomainAssert.notNull(internalResourceRepository, "Репозиторий внутренних ресурсов обязателен", "INTERNAL_RESOURCE_REPOSITORY_REQUIRED");
        this.internalResourceRepository = internalResourceRepository;
    }

    @Override
    public boolean execute(final Integer resourceId) {
        if (resourceId == null) {
            return false;
        }
        return internalResourceRepository.deleteById(resourceId);
    }
}
