package RUT.PlanningFlow.application.service.resource;

import RUT.PlanningFlow.application.port.in.resource.CreateInternalResourceUseCase;
import RUT.PlanningFlow.application.port.out.repository.InternalResourceRepositoryPort;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.InternalResource;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateInternalResourceService implements CreateInternalResourceUseCase {

    private final InternalResourceRepositoryPort internalResourceRepository;

    public CreateInternalResourceService(final InternalResourceRepositoryPort internalResourceRepository) {
        DomainAssert.notNull(internalResourceRepository, "Репозиторий внутренних ресурсов обязателен", "INTERNAL_RESOURCE_REPOSITORY_REQUIRED");
        this.internalResourceRepository = internalResourceRepository;
    }

    @Override
    public Integer execute(final String name, final ResourceType type, final String inventoryNumber) {
        DomainAssert.notBlank(name, "Название ресурса обязательно", "RESOURCE_NAME_REQUIRED");
        DomainAssert.notNull(type, "Тип ресурса обязателен", "RESOURCE_TYPE_REQUIRED");
        DomainAssert.notBlank(inventoryNumber, "Инвентарный номер обязателен", "INVENTORY_NUMBER_REQUIRED");

        final InternalResource resource = new InternalResource(null, name, type, inventoryNumber);
        return internalResourceRepository.create(resource)
                .orElseThrow(() -> new DomainException("Не удалось создать ресурс", "INTERNAL_RESOURCE_CREATE_FAILED"));
    }
}