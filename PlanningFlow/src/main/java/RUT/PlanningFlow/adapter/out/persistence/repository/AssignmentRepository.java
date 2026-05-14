package RUT.PlanningFlow.adapter.out.persistence.repository;

import RUT.PlanningFlow.adapter.out.persistence.entity.AssignmentEntity;
import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends BaseRepository<AssignmentEntity, Integer> {

    long countByStatus(AssignStatus status);

    Page<AssignmentEntity> findAllByOrderByAssignedAtDescIdDesc(Pageable pageable);
    Page<AssignmentEntity> findByTask_TitleContainingIgnoreCaseOrderByAssignedAtDescIdDesc(String searchTerm, Pageable pageable);
    Optional<AssignmentEntity> findFirstByTask_IdAndUser_IdAndStatusInOrderByAssignedAtDescIdDesc(Integer taskId, Integer userId, List<AssignStatus> statuses);

    Optional<AssignmentEntity> findFirstByTask_IdAndUser_IdOrderByAssignedAtDescIdDesc(Integer taskId, Integer userId);
    List<AssignmentEntity> findByTask_IdOrderByAssignedAtAscIdAsc(Integer taskId);

    @Query("""
            SELECT a FROM AssignmentEntity a
            JOIN FETCH a.task t
            JOIN FETCH t.event e
            WHERE a.user.id = :userId
            ORDER BY a.assignedAt DESC, a.id DESC
            """)
    List<AssignmentEntity> findAllForUserWithTaskAndEvent(@Param("userId") Integer userId);

    @Query("""
            SELECT a FROM AssignmentEntity a
            JOIN FETCH a.task t
            JOIN FETCH t.event e
            WHERE a.user.id = :participantId
            AND e.creator.id = :organizerId
            AND e.status IN :statuses
            ORDER BY e.startDate DESC, a.assignedAt DESC
            """)
    List<AssignmentEntity> findForParticipantUnderOrganizer(
            @Param("participantId") Integer participantId,
            @Param("organizerId") Integer organizerId,
            @Param("statuses") List<EventStatus> statuses
    );

    @Query("""
            SELECT DISTINCT a FROM AssignmentEntity a
            JOIN FETCH a.task t
            JOIN FETCH t.event e
            JOIN e.coordinators coord
            WHERE a.user.id = :participantId
            AND coord.id = :coordinatorId
            AND e.status IN :statuses
            ORDER BY e.startDate DESC, a.assignedAt DESC
            """)
    List<AssignmentEntity> findForParticipantUnderCoordinator(
            @Param("participantId") Integer participantId,
            @Param("coordinatorId") Integer coordinatorId,
            @Param("statuses") List<EventStatus> statuses
    );

    boolean existsByUser_IdAndTask_Event_Id(Integer userId, Integer eventId);

    boolean existsByUser_IdAndTask_Id(Integer userId, Integer taskId);

    
    boolean existsByUser_IdAndTask_IdAndStatusIn(Integer userId, Integer taskId, List<AssignStatus> statuses);

    @Query("""
            SELECT COUNT(DISTINCT t.id)
            FROM AssignmentEntity a
            JOIN a.task t
            WHERE a.user.id = :userId
              AND a.status = 'ACCEPTED'
              AND t.status = 'DONE'
            """)
    long countCompletedTasksForUser(@Param("userId") Integer userId);

    @Query("""
            SELECT COUNT(DISTINCT e.id)
            FROM AssignmentEntity a
            JOIN a.task t
            JOIN t.event e
            WHERE a.user.id = :userId
              AND a.status = 'ACCEPTED'
            """)
    long countDistinctEventsParticipatedForUser(@Param("userId") Integer userId);

    
    @Query("""
            SELECT COUNT(DISTINCT e.id) FROM AssignmentEntity a
            JOIN a.task t
            JOIN t.event e
            WHERE a.user.id = :participantId
            AND a.status = 'ACCEPTED'
            AND e.status IN :statuses
            AND (
                e.creator.id = :viewerId
                OR EXISTS (
                    SELECT 1 FROM UserEntity cu
                    WHERE cu.id = :viewerId AND cu MEMBER OF e.coordinators
                )
            )
            """)
    long countDistinctEventsForParticipantUnderViewer(
            @Param("participantId") Integer participantId,
            @Param("viewerId") Integer viewerId,
            @Param("statuses") List<EventStatus> statuses
    );

    
    @Query("""
            SELECT COUNT(DISTINCT e.id) FROM AssignmentEntity a
            JOIN a.task t
            JOIN t.event e
            WHERE a.user.id = :participantId
            AND a.status = 'ACCEPTED'
            """)
    long countDistinctEventsWithAssignmentsForParticipant(@Param("participantId") Integer participantId);

}