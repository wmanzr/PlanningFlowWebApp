package RUT.PlanningFlow.application.service.incident;

import RUT.PlanningFlow.application.dto.incident.IncidentResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.incident.GetIncidentsForEventQuery;
import RUT.PlanningFlow.application.port.out.repository.IncidentRepositoryPort;
import RUT.PlanningFlow.domain.model.Incident;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetIncidentsForEventService implements GetIncidentsForEventQuery {

    private final IncidentRepositoryPort incidentRepository;
    private final IncidentResponseDtoMapper incidentResponseDtoMapper;

    public GetIncidentsForEventService(
            final IncidentRepositoryPort incidentRepository,
            final IncidentResponseDtoMapper incidentResponseDtoMapper
    ) {
        DomainAssert.notNull(incidentRepository, "Репозиторий инцидентов обязателен", "INCIDENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(incidentResponseDtoMapper, "Маппер ответа по инциденту обязателен", "INCIDENT_RESPONSE_DTO_MAPPER_REQUIRED");
        this.incidentRepository = incidentRepository;
        this.incidentResponseDtoMapper = incidentResponseDtoMapper;
    }

    @Override
    public PageResult<IncidentResponseDto> execute(final Integer eventId, final PageQuery pageQuery) {
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");

        final PageResult<Incident> page = incidentRepository.findByEventId(eventId, pageQuery);

        final List<IncidentResponseDto> items = new ArrayList<>(page.items().size());
        for (final Incident i : page.items()) {
            items.add(incidentResponseDtoMapper.toResponse(i));
        }
        return new PageResult<>(items, page.totalElements(), page.totalPages());
    }
}