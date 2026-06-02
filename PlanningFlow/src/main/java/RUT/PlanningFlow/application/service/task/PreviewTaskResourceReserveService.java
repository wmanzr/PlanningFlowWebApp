package RUT.PlanningFlow.application.service.task;

import RUT.PlanningFlow.application.dto.resource.ResourceReservePreviewDto;
import RUT.PlanningFlow.application.port.in.task.PreviewTaskResourceReserveQuery;
import RUT.PlanningFlow.application.port.out.InternalResourceWarehousePort;
import RUT.PlanningFlow.application.port.out.repository.ResourceBookingRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.enums.BookingStatus;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.InternalResource;
import RUT.PlanningFlow.domain.model.Resource;
import RUT.PlanningFlow.domain.model.ResourceBooking;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import RUT.PlanningFlow.domain.vo.DateTimeRange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PreviewTaskResourceReserveService implements PreviewTaskResourceReserveQuery {

    private static final int WAREHOUSE_LIMIT = 1000;

    private final UserRepositoryPort userRepository;
    private final TaskRepositoryPort taskRepository;
    private final ResourceBookingRepositoryPort bookingRepository;
    private final InternalResourceWarehousePort internalWarehouse;

    public PreviewTaskResourceReserveService(
            final UserRepositoryPort userRepository,
            final TaskRepositoryPort taskRepository,
            final ResourceBookingRepositoryPort bookingRepository,
            final InternalResourceWarehousePort internalWarehouse
    ) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.bookingRepository = bookingRepository;
        this.internalWarehouse = internalWarehouse;
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceReservePreviewDto preview(
            final Integer callerUserId,
            final Integer taskId,
            final ResourceType resourceType,
            final String resourceName,
            final LocalDateTime reservedFrom,
            final LocalDateTime reservedTo,
            final int requiredCount
    ) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(taskId, "Задача обязательна", "TASK_ID_REQUIRED");
        DomainAssert.notNull(resourceType, "Тип ресурса обязателен", "RESOURCE_TYPE_REQUIRED");
        DomainAssert.notNull(reservedFrom, "Начало периода обязательно", "RESERVED_FROM_REQUIRED");
        DomainAssert.notNull(reservedTo, "Окончание периода обязательно", "RESERVED_TO_REQUIRED");
        if (!reservedTo.isAfter(reservedFrom)) {
            throw new DomainException("Завершение должно быть позже начала", "RESERVE_PREVIEW_INVALID_RANGE");
        }
        if (resourceName == null || resourceName.isBlank()) {
            throw new DomainException("Название ресурса обязательно", "RESOURCE_NAME_REQUIRED");
        }
        final int req = Math.min(Math.max(requiredCount, 1), 1000);

        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new DomainException("Пользователь не найден", "USER_NOT_FOUND"));
        final Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DomainException("Задача не найдена", "TASK_NOT_FOUND"));
        PlanningAccessPolicy.assertCanManageTaskAsPlanner(actor, task);

        final DateTimeRange window = new DateTimeRange(reservedFrom, reservedTo);
        final List<ResourceBooking> activeForTask = safeList(bookingRepository.findActiveForTask(taskId));
        final Set<Integer> bookedOnTask = activeBookedResourceIds(activeForTask, resourceType);

        final List<InternalResource> candidates = safeList(
                internalWarehouse.findAvailableOperationalByName(resourceType, resourceName.trim(), window, WAREHOUSE_LIMIT)
        );
        int usable = 0;
        for (final InternalResource r : candidates) {
            if (usable >= req) {
                break;
            }
            if (isUsable(r, bookedOnTask)) {
                usable++;
            }
        }

        final int fromInternal = Math.min(req, usable);
        final int fromExternal = req - fromInternal;
        return new ResourceReservePreviewDto(fromInternal, fromExternal);
    }

    private static boolean isUsable(final Resource resource, final Set<Integer> bookedOnTask) {
        if (resource == null || resource.getId() == null || !resource.isOperational()) {
            return false;
        }
        return !bookedOnTask.contains(resource.getId());
    }

    private static Set<Integer> activeBookedResourceIds(final List<ResourceBooking> activeForTask, final ResourceType type) {
        final Set<Integer> ids = new HashSet<>();
        for (final ResourceBooking b : activeForTask) {
            if (b == null || b.getResource() == null || !isActive(b.getStatus())) {
                continue;
            }
            final Resource r = b.getResource();
            if (r.getType() == type && r.getId() != null) {
                ids.add(r.getId());
            }
        }
        return ids;
    }

    private static boolean isActive(final BookingStatus status) {
        return status == BookingStatus.REQUESTED || status == BookingStatus.CONFIRMED;
    }

    private static <T> List<T> safeList(final List<T> value) {
        return value == null ? List.of() : value;
    }
}
