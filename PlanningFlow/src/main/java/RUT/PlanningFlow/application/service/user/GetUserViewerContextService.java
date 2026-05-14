package RUT.PlanningFlow.application.service.user;

import RUT.PlanningFlow.application.dto.user.EventBriefViewerDto;
import RUT.PlanningFlow.application.dto.user.UserViewerContextDto;
import RUT.PlanningFlow.application.port.in.user.GetUserViewerContextQuery;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Role;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetUserViewerContextService implements GetUserViewerContextQuery {

    private static final List<EventStatus> ACTIVE_EVENT_STATUSES = List.of(EventStatus.PLANNING, EventStatus.ACTIVE);

    
    private static final List<EventStatus> PARTICIPANT_VIEWER_EVENT_STATUSES =
            List.of(EventStatus.PLANNING, EventStatus.ACTIVE, EventStatus.COMPLETED);

    private final UserRepositoryPort userRepository;
    private final AssignmentRepositoryPort assignmentRepository;
    private final EventRepositoryPort eventRepository;

    public GetUserViewerContextService(
            final UserRepositoryPort userRepository,
            final AssignmentRepositoryPort assignmentRepository,
            final EventRepositoryPort eventRepository
    ) {
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(assignmentRepository, "Репозиторий назначений обязателен", "ASSIGNMENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public UserViewerContextDto execute(final int viewerUserId, final int targetUserId) {
        final User viewer = userRepository.findById(viewerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        final User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        final boolean organizer = hasRole(viewer, UserRoles.ORGANIZER);
        final boolean coordinatorViewer = hasRole(viewer, UserRoles.COORDINATOR);
        final boolean participantViewer = hasRole(viewer, UserRoles.PARTICIPANT);

        
        final boolean plannerViewer =
                organizer || coordinatorViewer || hasRole(viewer, UserRoles.ADMIN);

        
        if (!plannerViewer && participantViewer && hasRole(target, UserRoles.COORDINATOR)) {
            final List<Event> shared =
                    eventRepository.findEventsSharedBetweenCoordinatorAndParticipant(targetUserId, viewerUserId);
            final List<EventBriefViewerDto> coordinatorEvents = toBriefs(shared);
            final long sharedCount = shared.size();
            final long coordinatorTotalCount = eventRepository.countEventsWhereCoordinator(targetUserId);
            return new UserViewerContextDto(
                    List.of(),
                    List.of(),
                    coordinatorEvents,
                    null,
                    sharedCount,
                    coordinatorTotalCount,
                    null,
                    null
            );
        }

        if (!plannerViewer) {
            return new UserViewerContextDto(List.of(), List.of(), List.of(), null, null, null, null, null);
        }

        Long participantEventsUnderViewerCount = null;
        Long participantEventsTotalCount = null;
        if (hasRole(target, UserRoles.PARTICIPANT)) {
            participantEventsUnderViewerCount =
                    assignmentRepository.countDistinctEventsForParticipantUnderViewer(
                            targetUserId,
                            viewerUserId,
                            PARTICIPANT_VIEWER_EVENT_STATUSES
                    );
            participantEventsTotalCount =
                    assignmentRepository.countDistinctEventsWithAssignmentsForParticipant(targetUserId);
        }

        List<EventBriefViewerDto> coordinatorEvents = List.of();
        Long coordinatorUnderOrganizerCount = null;
        Long coordinatorTotalCount = null;
        if (hasRole(target, UserRoles.COORDINATOR)) {
            final List<Event> evs = eventRepository.findEventsWhereCoordinatorUnderOrganizer(
                    viewerUserId,
                    targetUserId,
                    ACTIVE_EVENT_STATUSES
            );
            coordinatorEvents = toBriefs(evs);
            coordinatorUnderOrganizerCount =
                    eventRepository.countEventsWhereCoordinatorUnderOrganizer(viewerUserId, targetUserId);
            coordinatorTotalCount = eventRepository.countEventsWhereCoordinator(targetUserId);
        }

        Long completedAsOrganizer = null;
        if (hasRole(target, UserRoles.ORGANIZER)) {
            completedAsOrganizer = eventRepository.countByCreatorIdAndStatus(targetUserId, EventStatus.COMPLETED);
        }

        return new UserViewerContextDto(
                List.of(),
                List.of(),
                coordinatorEvents,
                completedAsOrganizer,
                coordinatorUnderOrganizerCount,
                coordinatorTotalCount,
                participantEventsUnderViewerCount,
                participantEventsTotalCount
        );
    }

    private static boolean hasRole(final User user, final UserRoles role) {
        if (user == null || role == null) {
            return false;
        }
        for (final Role r : user.getRoles()) {
            if (r != null && role.equals(r.getName())) {
                return true;
            }
        }
        return false;
    }

    private static List<EventBriefViewerDto> toBriefs(final List<Event> events) {
        final List<EventBriefViewerDto> out = new ArrayList<>(events.size());
        for (final Event e : events) {
            if (e == null) {
                continue;
            }
            out.add(new EventBriefViewerDto(
                    e.getId(),
                    e.getTitle(),
                    e.getStatus().name(),
                    e.getStartDate(),
                    e.getEndDate()
            ));
        }
        return List.copyOf(out);
    }
}
