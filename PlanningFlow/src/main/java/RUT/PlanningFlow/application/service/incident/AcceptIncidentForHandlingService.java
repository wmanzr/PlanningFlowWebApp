package RUT.PlanningFlow.application.service.incident;

import RUT.PlanningFlow.application.port.in.incident.AcceptIncidentForHandlingUseCase;
import RUT.PlanningFlow.application.port.out.repository.IncidentRepositoryPort;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Incident;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AcceptIncidentForHandlingService implements AcceptIncidentForHandlingUseCase {

    private final IncidentRepositoryPort incidentRepository;

    public AcceptIncidentForHandlingService(final IncidentRepositoryPort incidentRepository) {
        DomainAssert.notNull(incidentRepository, "Репозиторий инцидентов обязателен", "INCIDENT_REPOSITORY_REQUIRED");
        this.incidentRepository = incidentRepository;
    }

    @Override
    public void execute(final Integer incidentId) {
        DomainAssert.notNull(incidentId, "ID инцидента обязателен", "INCIDENT_ID_REQUIRED");
        final Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new DomainException("Инцидент не найден", "INCIDENT_NOT_FOUND"));
        incident.markAsInProgress();
        incidentRepository.update(incident);
    }
}