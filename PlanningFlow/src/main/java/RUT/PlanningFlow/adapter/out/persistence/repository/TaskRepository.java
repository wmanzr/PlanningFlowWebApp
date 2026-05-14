package RUT.PlanningFlow.adapter.out.persistence.repository;

import RUT.PlanningFlow.adapter.out.persistence.entity.TaskEntity;
import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends BaseRepository<TaskEntity, Integer> {

    long countByStatus(TaskStatus status);
    Page<TaskEntity> findAllByOrderByStartTimeAscIdAsc(Pageable pageable);
    Page<TaskEntity> findByTitleContainingIgnoreCaseOrderByStartTimeAscIdAsc(String searchTerm, Pageable pageable);
    List<TaskEntity> findByEventIdOrderByStartTimeAscIdAsc(Integer eventId);

    boolean existsByEventIdAndTitleIgnoreCase(Integer eventId, String title);

    boolean existsByEventIdAndTitleIgnoreCaseAndIdNot(Integer eventId, String title, Integer id);
    Page<TaskEntity> findByEventIdOrderByStartTimeAscIdAsc(Integer eventId, Pageable pageable);
    Page<TaskEntity> findByEventIdAndStartTimeBetweenOrderByStartTimeAscIdAsc(Integer eventId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    
    @Query("""
            SELECT DISTINCT t
            FROM AssignmentEntity a
            JOIN a.task t
            WHERE a.user.id = :userId
              AND a.status IN :statuses
            ORDER BY t.startTime ASC, t.id ASC
            """)
    Page<TaskEntity> findTasksForUser(
            @Param("userId") Integer userId,
            @Param("statuses") List<AssignStatus> statuses,
            Pageable pageable
    );

    
    @Query(
            value = """
                    SELECT t.*
                    FROM tasks t
                    WHERE t.id IN (
                        SELECT DISTINCT t2.id
                        FROM tasks t2
                        JOIN assignments ae ON ae.task_id = t2.id
                        WHERE ae.user_id = :userId
                          AND ae.status IN (:statuses)
                          AND convert_from(t2.title, 'UTF8') ILIKE CONCAT('%', CAST(:title AS text), '%')
                    )
                    ORDER BY t.start_time ASC, t.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*) FROM (
                        SELECT DISTINCT t2.id
                        FROM tasks t2
                        JOIN assignments ae ON ae.task_id = t2.id
                        WHERE ae.user_id = :userId
                          AND ae.status IN (:statuses)
                          AND convert_from(t2.title, 'UTF8') ILIKE CONCAT('%', CAST(:title AS text), '%')
                    ) ids
                    """,
            nativeQuery = true
    )
    Page<TaskEntity> findTasksForUserWithTitle(
            @Param("userId") Integer userId,
            @Param("statuses") List<AssignStatus> statuses,
            @Param("title") String title,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT t
            FROM AssignmentEntity a
            JOIN a.task t
            WHERE a.user.id = :userId
              AND a.status IN :statuses
              AND t.startTime >= :start
              AND t.startTime < :end
            ORDER BY t.startTime ASC, t.id ASC
            """)
    Page<TaskEntity> findTasksForUserBetween(
            @Param("userId") Integer userId,
            @Param("statuses") List<AssignStatus> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT t.*
                    FROM tasks t
                    WHERE t.id IN (
                        SELECT DISTINCT t2.id
                        FROM tasks t2
                        JOIN assignments ae ON ae.task_id = t2.id
                        WHERE ae.user_id = :userId
                          AND ae.status IN (:statuses)
                          AND t2.start_time >= :start
                          AND t2.start_time < :end
                          AND convert_from(t2.title, 'UTF8') ILIKE CONCAT('%', CAST(:title AS text), '%')
                    )
                    ORDER BY t.start_time ASC, t.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*) FROM (
                        SELECT DISTINCT t2.id
                        FROM tasks t2
                        JOIN assignments ae ON ae.task_id = t2.id
                        WHERE ae.user_id = :userId
                          AND ae.status IN (:statuses)
                          AND t2.start_time >= :start
                          AND t2.start_time < :end
                          AND convert_from(t2.title, 'UTF8') ILIKE CONCAT('%', CAST(:title AS text), '%')
                    ) ids
                    """,
            nativeQuery = true
    )
    Page<TaskEntity> findTasksForUserBetweenWithTitle(
            @Param("userId") Integer userId,
            @Param("statuses") List<AssignStatus> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("title") String title,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT t
            FROM AssignmentEntity a
            JOIN a.task t
            WHERE a.user.id = :userId
              AND a.status IN :assignmentStatuses
              AND t.status NOT IN :excludedTaskStatuses
              AND t.startTime >= :start
              AND t.startTime < :end
            """)
    List<TaskEntity> findCommittedTasksForUserOnDate(
            @Param("userId") Integer userId,
            @Param("assignmentStatuses") List<AssignStatus> assignmentStatuses,
            @Param("excludedTaskStatuses") List<TaskStatus> excludedTaskStatuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    
    @Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.createdBy.id = :userId")
    long countTasksAuthoredByUser(@Param("userId") Integer userId);

    @Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.event.id = :eventId AND t.status = :status")
    long countByEventIdAndStatus(@Param("eventId") Integer eventId, @Param("status") TaskStatus status);

    @Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.event.id = :eventId")
    long countTasksForEvent(@Param("eventId") Integer eventId);

    @Query(
            value = """
                    SELECT COALESCE(SUM(EXTRACT(EPOCH FROM (t.end_time - t.start_time))), 0) / 3600.0
                    FROM tasks t
                    JOIN assignments a ON a.task_id = t.id
                    WHERE a.user_id = :userId
                      AND a.status = 'ACCEPTED'
                      AND t.status = 'DONE'
                      AND t.start_time IS NOT NULL
                      AND t.end_time IS NOT NULL
                    """,
            nativeQuery = true
    )
    Double sumCompletedWorkedHours(@Param("userId") Integer userId);
}