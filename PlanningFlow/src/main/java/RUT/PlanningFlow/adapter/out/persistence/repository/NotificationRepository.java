package RUT.PlanningFlow.adapter.out.persistence.repository;

import RUT.PlanningFlow.adapter.out.persistence.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends BaseRepository<NotificationEntity, Integer> {

    @Query("""
            SELECT n
            FROM NotificationEntity n
            WHERE n.user.id = :userId
            ORDER BY n.createdAt DESC, n.id DESC
            """)
    Page<NotificationEntity> findByUser(@Param("userId") Integer userId, Pageable pageable);

    @Query("""
            SELECT n
            FROM NotificationEntity n
            WHERE n.user.id = :userId
              AND n.readAt IS NULL
            ORDER BY n.createdAt DESC, n.id DESC
            """)
    Page<NotificationEntity> findUnreadByUser(@Param("userId") Integer userId, Pageable pageable);

    @Query("""
            SELECT n
            FROM NotificationEntity n
            WHERE n.user.id = :userId
              AND n.readAt IS NOT NULL
            ORDER BY n.createdAt DESC, n.id DESC
            """)
    Page<NotificationEntity> findReadByUser(@Param("userId") Integer userId, Pageable pageable);

    @Query("""
            SELECT COUNT(n)
            FROM NotificationEntity n
            WHERE n.user.id = :userId
              AND n.readAt IS NULL
            """)
    long countUnread(@Param("userId") Integer userId);

    @Modifying
    @Query("""
            UPDATE NotificationEntity n
            SET n.readAt = CURRENT_TIMESTAMP
            WHERE n.id = :id
              AND n.user.id = :userId
              AND n.readAt IS NULL
            """)
    int markRead(@Param("userId") Integer userId, @Param("id") Integer id);

    @Modifying
    @Query("""
            UPDATE NotificationEntity n
            SET n.readAt = CURRENT_TIMESTAMP
            WHERE n.user.id = :userId
              AND n.readAt IS NULL
            """)
    int markAllRead(@Param("userId") Integer userId);
}

