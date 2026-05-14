package RUT.PlanningFlow.adapter.out.persistence.repository;

import RUT.PlanningFlow.adapter.out.persistence.entity.EventEntity;
import RUT.PlanningFlow.domain.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends BaseRepository<EventEntity, Integer> {

    long countByStatus(EventStatus status);

    @Query("SELECT COUNT(e) FROM EventEntity e")
    long countAllEvents();

    @Query("""
            SELECT DISTINCT e FROM EventEntity e
            LEFT JOIN FETCH e.creator
            LEFT JOIN FETCH e.coordinators
            WHERE e.id = :id
            """)
    Optional<EventEntity> findByIdEagerCreatorAndCoordinators(@Param("id") Integer id);

    Page<EventEntity> findAllByOrderByStartDateAscIdAsc(Pageable pageable);
    Page<EventEntity> findByTitleContainingIgnoreCaseOrderByStartDateAscIdAsc(String searchTerm, Pageable pageable);
    Page<EventEntity> findByStartDateBetweenOrderByStartDateAscIdAsc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<EventEntity> findByCreator_IdOrderByStartDateAscIdAsc(Integer creatorId, Pageable pageable);
    Page<EventEntity> findByCreator_IdAndStartDateBetweenOrderByStartDateAscIdAsc(
            Integer creatorId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Page<EventEntity> findByCreator_IdAndTitleContainingIgnoreCaseOrderByStartDateAscIdAsc(
            Integer creatorId,
            String title,
            Pageable pageable
    );

    Page<EventEntity> findByCreator_IdAndTitleContainingIgnoreCaseAndStartDateBetweenOrderByStartDateAscIdAsc(
            Integer creatorId,
            String title,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Page<EventEntity> findByTitleContainingIgnoreCaseAndStartDateBetweenOrderByStartDateAscIdAsc(
            String title,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(e) FROM EventEntity e
            WHERE e.creator.id = :creatorId AND e.status = :status
            """)
    long countByCreatorIdAndStatus(@Param("creatorId") Integer creatorId, @Param("status") EventStatus status);

    @Query("""
            SELECT COUNT(DISTINCT e) FROM EventEntity e
            JOIN e.coordinators c
            WHERE c.id = :coordinatorId
            """)
    long countWhereCoordinator(@Param("coordinatorId") Integer coordinatorId);

    @Query("""
            SELECT COUNT(DISTINCT e) FROM EventEntity e
            JOIN e.coordinators c
            WHERE e.creator.id = :organizerId
            AND c.id = :coordinatorId
            """)
    long countWhereCoordinatorUnderOrganizer(
            @Param("organizerId") Integer organizerId,
            @Param("coordinatorId") Integer coordinatorId
    );

    @Query("""
            SELECT DISTINCT e FROM EventEntity e
            JOIN e.coordinators c
            WHERE e.creator.id = :organizerId
            AND c.id = :coordinatorId
            AND e.status IN :statuses
            ORDER BY e.startDate ASC
            """)
    List<EventEntity> findEventsWhereCoordinatorUnderOrganizer(
            @Param("organizerId") Integer organizerId,
            @Param("coordinatorId") Integer coordinatorId,
            @Param("statuses") List<EventStatus> statuses
    );

    @Query("""
            SELECT DISTINCT e FROM EventEntity e
            JOIN e.coordinators coord
            WHERE coord.id = :coordinatorId
            AND EXISTS (
                SELECT 1 FROM AssignmentEntity a
                JOIN a.task t
                WHERE t.event = e AND a.user.id = :participantId
            )
            ORDER BY e.startDate DESC
            """)
    List<EventEntity> findEventsSharedBetweenCoordinatorAndParticipant(
            @Param("coordinatorId") Integer coordinatorId,
            @Param("participantId") Integer participantId
    );

    @Query("""
            SELECT DISTINCT e FROM EventEntity e
            LEFT JOIN e.coordinators coord
            WHERE e.creator.id = :userId OR coord.id = :userId
            ORDER BY e.startDate ASC, e.id ASC
            """)
    Page<EventEntity> findAccessibleByUser(@Param("userId") Integer userId, Pageable pageable);

    @Query("""
            SELECT DISTINCT e FROM EventEntity e
            JOIN e.coordinators coord
            WHERE coord.id = :userId
            ORDER BY e.startDate ASC, e.id ASC
            """)
    Page<EventEntity> findByCoordinator(@Param("userId") Integer userId, Pageable pageable);

    @Query("""
            SELECT DISTINCT e FROM EventEntity e
            LEFT JOIN e.coordinators coord
            WHERE (e.creator.id = :userId OR coord.id = :userId)
            AND e.startDate BETWEEN :start AND :end
            ORDER BY e.startDate ASC, e.id ASC
            """)
    Page<EventEntity> findAccessibleByUserAndStartDateBetween(
            @Param("userId") Integer userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT e FROM EventEntity e
            JOIN e.coordinators coord
            WHERE coord.id = :userId
            AND e.startDate BETWEEN :start AND :end
            ORDER BY e.startDate ASC, e.id ASC
            """)
    Page<EventEntity> findByCoordinatorAndStartDateBetween(
            @Param("userId") Integer userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT e FROM EventEntity e
            JOIN e.coordinators coord
            WHERE coord.id = :userId
            AND LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))
            ORDER BY e.startDate ASC, e.id ASC
            """)
    Page<EventEntity> findByCoordinatorAndTitleContaining(
            @Param("userId") Integer userId,
            @Param("title") String title,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT e FROM EventEntity e
            JOIN e.coordinators coord
            WHERE coord.id = :userId
            AND LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))
            AND e.startDate BETWEEN :start AND :end
            ORDER BY e.startDate ASC, e.id ASC
            """)
    Page<EventEntity> findByCoordinatorAndTitleContainingAndStartDateBetween(
            @Param("userId") Integer userId,
            @Param("title") String title,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT e FROM EventEntity e
            LEFT JOIN e.coordinators coord
            WHERE (e.creator.id = :userId OR coord.id = :userId)
            AND LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))
            ORDER BY e.startDate ASC, e.id ASC
            """)
    Page<EventEntity> findAccessibleByUserAndTitleContaining(
            @Param("userId") Integer userId,
            @Param("title") String title,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT e FROM EventEntity e
            LEFT JOIN e.coordinators coord
            WHERE (e.creator.id = :userId OR coord.id = :userId)
            AND LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))
            AND e.startDate BETWEEN :start AND :end
            ORDER BY e.startDate ASC, e.id ASC
            """)
    Page<EventEntity> findAccessibleByUserAndTitleContainingAndStartDateBetween(
            @Param("userId") Integer userId,
            @Param("title") String title,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(DISTINCT e.id) FROM EventEntity e
            LEFT JOIN e.coordinators c
            WHERE e.status = 'COMPLETED'
              AND (e.creator.id = :userId OR c.id = :userId)
            """)
    long countCompletedEventsWhereCreatorOrCoordinator(@Param("userId") Integer userId);

    @Query("SELECT COUNT(e) FROM EventEntity e WHERE e.creator.id = :creatorId")
    long countEventsCreatedByUser(@Param("creatorId") Integer creatorId);
}