package RUT.PlanningFlow.application.port.out.repository;

import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.model.Assignment;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepositoryPort {
    Optional<Assignment> findById(Integer id);
    Optional<Assignment> findActiveForTaskAndUser(Integer taskId, Integer userId);

    
    Optional<Assignment> findLatestForTaskAndUser(Integer taskId, Integer userId);
    List<Assignment> findByTaskId(Integer taskId);
    List<Assignment> findAll();
    PageResult<Assignment> findAssignments(PageQuery pageQuery);
    PageResult<Assignment> findByTaskTitleContainingIgnoreCase(String searchTerm, PageQuery pageQuery);
    List<Assignment> findAllWithTaskAndEventForUser(Integer userId);
    List<Assignment> findForParticipantUnderOrganizer(Integer participantId, Integer organizerId, List<EventStatus> statuses);

    
    List<Assignment> findForParticipantUnderCoordinator(Integer participantId, Integer coordinatorId, List<EventStatus> statuses);
    boolean existsAssignmentForUserOnEvent(Integer userId, Integer eventId);

    boolean existsAssignmentForUserOnTask(Integer userId, Integer taskId);

    long countCompletedTasksForUser(Integer userId);

    long countDistinctEventsParticipatedForUser(Integer userId);

    long countDistinctEventsForParticipantUnderViewer(Integer participantId, Integer viewerId, List<EventStatus> statuses);

    long countDistinctEventsWithAssignmentsForParticipant(Integer participantId);

    long countByStatus(AssignStatus status);

    Optional<Integer> create(Assignment assignment);
    Optional<Integer> update(Assignment assignment);
}