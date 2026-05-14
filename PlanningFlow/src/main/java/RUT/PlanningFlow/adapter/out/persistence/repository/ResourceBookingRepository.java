package RUT.PlanningFlow.adapter.out.persistence.repository;

import RUT.PlanningFlow.adapter.out.persistence.entity.ResourceBookingEntity;
import RUT.PlanningFlow.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ResourceBookingRepository extends BaseRepository<ResourceBookingEntity, Integer> {
    void deleteByResource_Id(Integer resourceId);

    Page<ResourceBookingEntity> findAllByOrderByReservedFromAscIdAsc(Pageable pageable);
    Page<ResourceBookingEntity> findByResource_NameContainingIgnoreCaseOrderByReservedFromAscIdAsc(String searchTerm, Pageable pageable);
    List<ResourceBookingEntity> findByTask_IdAndStatusIn(Integer taskId, List<BookingStatus> statuses);
    Page<ResourceBookingEntity> findByTask_IdOrderByReservedFromAscIdAsc(Integer taskId, Pageable pageable);

    @Query("""
            SELECT DISTINCT rb.resource.id FROM ResourceBookingEntity rb
            WHERE rb.resource.id IN :resourceIds
            AND rb.status IN :statuses
            AND rb.reservedFrom IS NOT NULL AND rb.reservedTo IS NOT NULL
            AND rb.reservedFrom < :windowEnd AND rb.reservedTo > :windowStart
            """)
    List<Integer> findResourceIdsWithOverlappingActiveBookings(
            @Param("resourceIds") Collection<Integer> resourceIds,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd
    );

    @Query("""
            SELECT COUNT(DISTINCT rb.id) FROM ResourceBookingEntity rb
            JOIN rb.task t
            JOIN t.event e
            LEFT JOIN e.coordinators c
            WHERE e.creator.id = :userId OR c.id = :userId
            """)
    long countBookingsForEventsWhereCreatorOrCoordinator(@Param("userId") Integer userId);

    @Query("""
            SELECT COUNT(DISTINCT rb.id) FROM ResourceBookingEntity rb
            JOIN rb.task t
            JOIN t.event e
            WHERE e.creator.id = :organizerId
            """)
    long countBookingsForEventsWhereCreator(@Param("organizerId") Integer organizerId);
}