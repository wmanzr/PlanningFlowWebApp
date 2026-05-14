package RUT.PlanningFlow.application.port.out.repository;

import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.domain.model.ResourceBooking;

import java.util.List;
import java.util.Optional;

public interface ResourceBookingRepositoryPort {
    Optional<ResourceBooking> findById(Integer id);
    List<ResourceBooking> findAll();
    PageResult<ResourceBooking> findResourceBookings(PageQuery pageQuery);
    PageResult<ResourceBooking> findByResourceNameContainingIgnoreCase(String searchTerm, PageQuery pageQuery);
    Optional<Integer> create(ResourceBooking booking);
    Optional<Integer> update(ResourceBooking booking);
    List<ResourceBooking> findActiveForTask(Integer taskId);
    PageResult<ResourceBooking> findByTaskId(Integer taskId, PageQuery pageQuery);

    long countBookingsForEventsWhereCreatorOrCoordinator(Integer userId);

    long countBookingsForEventsWhereCreator(Integer organizerId);
}