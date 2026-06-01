package RUT.PlanningFlow.application.service.incident;

import RUT.PlanningFlow.application.dto.incident.IncidentResponseDto;
import RUT.PlanningFlow.application.port.in.incident.GetIncidentDetailsQuery;
import RUT.PlanningFlow.application.port.out.repository.IncidentRepositoryPort;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GetIncidentDetailsService implements GetIncidentDetailsQuery {

    private final IncidentRepositoryPort incidentRepository;
    private final IncidentResponseDtoMapper incidentResponseDtoMapper;

    public GetIncidentDetailsService(
            final IncidentRepositoryPort incidentRepository,
            final IncidentResponseDtoMapper incidentResponseDtoMapper
    ) {
        DomainAssert.notNull(incidentRepository, "Репозиторий инцидентов обязателен", "INCIDENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(incidentResponseDtoMapper, "Маппер ответа по инциденту обязателен", "INCIDENT_RESPONSE_DTO_MAPPER_REQUIRED");
        this.incidentRepository = incidentRepository;
        this.incidentResponseDtoMapper = incidentResponseDtoMapper;
    }

    @Override
    public Optional<IncidentResponseDto> execute(final Integer incidentId) {
        DomainAssert.notNull(incidentId, "ID инцидента обязателен", "INCIDENT_ID_REQUIRED");
        return incidentRepository.findById(incidentId).map(incidentResponseDtoMapper::toResponse);
    }
}
