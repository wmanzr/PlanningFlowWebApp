package RUT.PlanningFlow.application.port.out.repository;

import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.model.Event;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepositoryPort {
    Optional<Event> findById(Integer id);
    List<Event> findAll();
    PageResult<Event> findEvents(PageQuery pageQuery);
    PageResult<Event> findByTitleContainingIgnoreCase(String searchTerm, PageQuery pageQuery);
    PageResult<Event> findByStartDateBetween(LocalDateTime start, LocalDateTime end, PageQuery pageQuery);
    PageResult<Event> findAccessibleByUser(Integer userId, PageQuery pageQuery);
    PageResult<Event> findAccessibleByUserAndStartDateBetween(Integer userId, LocalDateTime start, LocalDateTime end, PageQuery pageQuery);
    PageResult<Event> findByCreator(Integer creatorId, PageQuery pageQuery);
    PageResult<Event> findByCreatorAndStartDateBetween(Integer creatorId, LocalDateTime start, LocalDateTime end, PageQuery pageQuery);
    PageResult<Event> findByCoordinator(Integer coordinatorId, PageQuery pageQuery);
    PageResult<Event> findByCoordinatorAndStartDateBetween(Integer coordinatorId, LocalDateTime start, LocalDateTime end, PageQuery pageQuery);

    PageResult<Event> findByTitleContainingIgnoreCaseAndStartDateBetween(
            String title,
            LocalDateTime start,
            LocalDateTime end,
            PageQuery pageQuery
    );

    PageResult<Event> findByCreatorAndTitleContainingIgnoreCase(Integer creatorId, String title, PageQuery pageQuery);

    PageResult<Event> findByCreatorAndTitleContainingIgnoreCaseAndStartDateBetween(
            Integer creatorId,
            String title,
            LocalDateTime start,
            LocalDateTime end,
            PageQuery pageQuery
    );

    PageResult<Event> findByCoordinatorAndTitleContainingIgnoreCase(Integer coordinatorId, String title, PageQuery pageQuery);

    PageResult<Event> findByCoordinatorAndTitleContainingIgnoreCaseAndStartDateBetween(
            Integer coordinatorId,
            String title,
            LocalDateTime start,
            LocalDateTime end,
            PageQuery pageQuery
    );

    PageResult<Event> findAccessibleByUserAndTitleContainingIgnoreCase(Integer userId, String title, PageQuery pageQuery);

    PageResult<Event> findAccessibleByUserAndTitleContainingIgnoreCaseAndStartDateBetween(
            Integer userId,
            String title,
            LocalDateTime start,
            LocalDateTime end,
            PageQuery pageQuery
    );

    long countByCreatorIdAndStatus(Integer creatorId, EventStatus status);

    long countByStatus(EventStatus status);

    long countAllEvents();
    List<Event> findEventsWhereCoordinatorUnderOrganizer(Integer organizerId, Integer coordinatorId, List<EventStatus> statuses);

    
    List<Event> findEventsSharedBetweenCoordinatorAndParticipant(Integer coordinatorId, Integer participantId);
    long countEventsWhereCoordinator(Integer coordinatorId);
    long countEventsWhereCoordinatorUnderOrganizer(Integer organizerId, Integer coordinatorId);
    long countCompletedEventsWhereCreatorOrCoordinator(Integer userId);
    long countEventsCreatedByUser(Integer creatorId);
    Optional<Integer> create(Event event);
    Optional<Integer> update(Event event);
}