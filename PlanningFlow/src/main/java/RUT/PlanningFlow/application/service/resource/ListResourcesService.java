package RUT.PlanningFlow.application.service.resource;

import RUT.PlanningFlow.application.dto.resource.InternalResourceResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.resource.ListResourcesQuery;
import RUT.PlanningFlow.application.port.out.repository.InternalResourceRepositoryPort;
import RUT.PlanningFlow.domain.model.InternalResource;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListResourcesService implements ListResourcesQuery {

    private final InternalResourceRepositoryPort internalResourceRepository;

    public ListResourcesService(final InternalResourceRepositoryPort internalResourceRepository) {
        DomainAssert.notNull(internalResourceRepository, "Репозиторий внутренних ресурсов обязателен", "INTERNAL_RESOURCE_REPOSITORY_REQUIRED");
        this.internalResourceRepository = internalResourceRepository;
    }

    @Override
    public PageResult<InternalResourceResponseDto> execute(final String name, final PageQuery pageQuery) {
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");

        final PageResult<InternalResource> page = name == null
                ? internalResourceRepository.findInternalResources(pageQuery)
                : internalResourceRepository.findByNameContainingIgnoreCase(name, pageQuery);

        final List<InternalResourceResponseDto> items = new ArrayList<>(page.items().size());
        for (final InternalResource r : page.items()) {
            items.add(InternalResourceResponseDto.from(r));
        }
        return new PageResult<>(items, page.totalElements(), page.totalPages());
    }
}