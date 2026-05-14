package RUT.PlanningFlow.application.service.landing;

import RUT.PlanningFlow.application.dto.landing.PublicLandingStatsDto;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.IncidentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.enums.IncidentStatus;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import org.springframework.stereotype.Service;

@Service
public class GetPublicLandingStatsService {

    private final EventRepositoryPort eventRepository;
    private final TaskRepositoryPort taskRepository;
    private final UserRepositoryPort userRepository;
    private final IncidentRepositoryPort incidentRepository;
    private final AssignmentRepositoryPort assignmentRepository;

    public GetPublicLandingStatsService(
            final EventRepositoryPort eventRepository,
            final TaskRepositoryPort taskRepository,
            final UserRepositoryPort userRepository,
            final IncidentRepositoryPort incidentRepository,
            final AssignmentRepositoryPort assignmentRepository
    ) {
        this.eventRepository = eventRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.incidentRepository = incidentRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public PublicLandingStatsDto getLandingStats() {
        return new PublicLandingStatsDto(
                eventRepository.countAllEvents(),
                eventRepository.countByStatus(EventStatus.COMPLETED),
                taskRepository.countByStatus(TaskStatus.DONE),
                userRepository.countAllUsers(),
                incidentRepository.countByStatus(IncidentStatus.RESOLVED),
                assignmentRepository.countByStatus(AssignStatus.ACCEPTED)
        );
    }
}
