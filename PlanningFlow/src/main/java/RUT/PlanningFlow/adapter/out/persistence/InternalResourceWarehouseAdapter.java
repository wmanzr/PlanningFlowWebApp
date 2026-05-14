package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.InternalResourceEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.InternalResourceRepository;
import RUT.PlanningFlow.adapter.out.persistence.repository.ResourceBookingRepository;
import RUT.PlanningFlow.application.port.out.InternalResourceWarehousePort;
import RUT.PlanningFlow.domain.enums.BookingStatus;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.model.InternalResource;
import RUT.PlanningFlow.domain.vo.DateTimeRange;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class InternalResourceWarehouseAdapter implements InternalResourceWarehousePort {

    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES = List.of(
            BookingStatus.REQUESTED,
            BookingStatus.CONFIRMED
    );

    private final InternalResourceRepository internalResourceRepository;
    private final ResourceBookingRepository resourceBookingRepository;

    public InternalResourceWarehouseAdapter(
            final InternalResourceRepository internalResourceRepository,
            final ResourceBookingRepository resourceBookingRepository
    ) {
        this.internalResourceRepository = internalResourceRepository;
        this.resourceBookingRepository = resourceBookingRepository;
    }

    @Override
    public List<InternalResource> findAvailableOperationalByName(
            final ResourceType type,
            final String resourceName,
            final DateTimeRange window,
            final int limit
    ) {
        if (limit <= 0 || resourceName == null || resourceName.isBlank() || window == null) {
            return List.of();
        }
        final String name = resourceName.trim();
        final List<InternalResourceEntity> candidates =
                internalResourceRepository.findByTypeAndOperationalTrueAndNameIgnoreCase(type, name);
        if (candidates.isEmpty()) {
            return List.of();
        }
        final List<Integer> ids = new ArrayList<>(candidates.size());
        for (final InternalResourceEntity e : candidates) {
            ids.add(e.getId());
        }
        final List<Integer> busyIds = ids.isEmpty()
                ? List.of()
                : resourceBookingRepository.findResourceIdsWithOverlappingActiveBookings(
                        ids,
                        ACTIVE_BOOKING_STATUSES,
                        window.getStart(),
                        window.getEnd()
                );
        final Set<Integer> busy = new HashSet<>(busyIds);
        final List<InternalResource> result = new ArrayList<>(Math.min(limit, candidates.size()));
        for (final InternalResourceEntity e : candidates) {
            if (result.size() >= limit) {
                break;
            }
            if (!busy.contains(e.getId())) {
                result.add((InternalResource) EntityToDomainMapper.toDomain(e));
            }
        }
        return List.copyOf(result);
    }
}