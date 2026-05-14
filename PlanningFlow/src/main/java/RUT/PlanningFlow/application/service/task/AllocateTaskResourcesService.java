package RUT.PlanningFlow.application.service.task;

import RUT.PlanningFlow.application.dto.resource.ReserveResourcesResponseDto;
import RUT.PlanningFlow.application.dto.resource.ResourceBookingResponseDto;
import RUT.PlanningFlow.application.port.in.task.AllocateTaskResourcesUseCase;
import RUT.PlanningFlow.application.port.out.EquipmentRentalPort;
import RUT.PlanningFlow.application.port.out.InternalResourceWarehousePort;
import RUT.PlanningFlow.application.port.out.TransportLogisticsPort;
import RUT.PlanningFlow.application.port.out.repository.ExternalResourceRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.ResourceBookingRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.enums.BookingStatus;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.ExternalResource;
import RUT.PlanningFlow.domain.model.InternalResource;
import RUT.PlanningFlow.domain.model.Resource;
import RUT.PlanningFlow.domain.model.ResourceBooking;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import RUT.PlanningFlow.domain.vo.DateTimeRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class AllocateTaskResourcesService implements AllocateTaskResourcesUseCase {

    private static final Logger log = LoggerFactory.getLogger(AllocateTaskResourcesService.class);

    private final TaskRepositoryPort taskRepository;
    private final InternalResourceWarehousePort internalWarehouse;
    private final TransportLogisticsPort transportLogistics;
    private final EquipmentRentalPort equipmentRental;
    private final ExternalResourceRepositoryPort externalResourceRepository;
    private final ResourceBookingRepositoryPort bookingRepository;
    private final UserRepositoryPort userRepository;
    private final TransactionTemplate transactionTemplate;

    private record Phase1Result(List<ResourceBooking> allCreated, List<ResourceBooking> pendingExternals) {}

    public AllocateTaskResourcesService(
            final TaskRepositoryPort taskRepository,
            final InternalResourceWarehousePort internalWarehouse,
            final TransportLogisticsPort transportLogistics,
            final EquipmentRentalPort equipmentRental,
            final ExternalResourceRepositoryPort externalResourceRepository,
            final ResourceBookingRepositoryPort bookingRepository,
            final UserRepositoryPort userRepository,
            final TransactionTemplate transactionTemplate
    ) {
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(internalWarehouse, "Внутренний склад обязателен", "INTERNAL_WAREHOUSE_REQUIRED");
        DomainAssert.notNull(transportLogistics, "Логистический сервис обязателен", "TRANSPORT_LOGISTICS_REQUIRED");
        DomainAssert.notNull(equipmentRental, "Сервис аренды обязателен", "EQUIPMENT_RENTAL_REQUIRED");
        DomainAssert.notNull(externalResourceRepository, "Репозиторий внешних ресурсов обязателен", "EXTERNAL_RESOURCE_REPOSITORY_REQUIRED");
        DomainAssert.notNull(bookingRepository, "Репозиторий бронирований обязателен", "RESOURCE_BOOKING_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(transactionTemplate, "TransactionTemplate обязателен", "TRANSACTION_TEMPLATE_REQUIRED");
        this.taskRepository = taskRepository;
        this.internalWarehouse = internalWarehouse;
        this.transportLogistics = transportLogistics;
        this.equipmentRental = equipmentRental;
        this.externalResourceRepository = externalResourceRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public ReserveResourcesResponseDto execute(
            final Integer callerUserId,
            final Integer taskId,
            final ResourceType type,
            final String resourceName,
            final int requiredCount,
            final LocalDateTime reservedFrom,
            final LocalDateTime reservedTo
    ) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(taskId, "Задача обязательна", "TASK_ID_REQUIRED");
        DomainAssert.notNull(type, "Тип ресурса обязателен", "RESOURCE_TYPE_REQUIRED");
        DomainAssert.notBlank(resourceName, "Название ресурса обязательно", "RESOURCE_NAME_REQUIRED");
        DomainAssert.isTrue(requiredCount > 0, "Требуемое количество должно быть положительным", "INVALID_REQUIRED_COUNT");

        final Phase1Result phase1 = transactionTemplate.execute(status -> {
            final User actor = userRepository.findById(callerUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            final Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new DomainException("Задача не найдена", "TASK_NOT_FOUND"));
            PlanningAccessPolicy.assertCanManageTaskAsPlanner(actor, task);

            final DateTimeRange window = new DateTimeRange(reservedFrom, reservedTo);
            final List<ResourceBooking> activeForTask = safeList(bookingRepository.findActiveForTask(taskId));
            final Set<Integer> alreadyBookedResourceIds = collectActiveBookedResourceIds(activeForTask, type);
            
            final int remaining = requiredCount;

            final List<ResourceBooking> createdBookings = new ArrayList<>();
            final List<ResourceBooking> pendingExtBookings = new ArrayList<>();

            if (remaining <= 0) {
                return new Phase1Result(createdBookings, pendingExtBookings);
            }

            final List<InternalResource> internal = safeList(
                    internalWarehouse.findAvailableOperationalByName(type, resourceName, window, remaining)
            );
            for (final InternalResource r : internal) {
                if (createdBookings.size() >= remaining) {
                    break;
                }
                if (!isUsable(r, alreadyBookedResourceIds)) {
                    continue;
                }
                final ResourceBooking booking = new ResourceBooking(null, task, r, BookingStatus.CONFIRMED, reservedFrom, reservedTo);
                final Integer bookingId = bookingRepository.create(booking)
                        .orElseThrow(() -> new DomainException("Не удалось сохранить бронирование", "BOOKING_CREATE_FAILED"));
                final ResourceBooking saved = bookingRepository.findById(bookingId)
                        .orElseThrow(() -> new DomainException("Не удалось загрузить бронирование", "BOOKING_LOAD_FAILED"));
                createdBookings.add(saved);
                alreadyBookedResourceIds.add(r.getId());
            }

            while (createdBookings.size() < remaining) {
                final String pendingApiId = "PENDING-" + UUID.randomUUID();
                final ExternalResource pendingResource = new ExternalResource(null, resourceName, type, pendingApiId);
                final Integer resourceId = externalResourceRepository.create(pendingResource)
                        .orElseThrow(() -> new DomainException("Не удалось сохранить внешний ресурс", "EXTERNAL_RESOURCE_CREATE_FAILED"));
                final ExternalResource savedResource = new ExternalResource(resourceId, resourceName, type, pendingApiId);

                final ResourceBooking booking = new ResourceBooking(null, task, savedResource, BookingStatus.REQUESTED, reservedFrom, reservedTo);
                final Integer bookingId = bookingRepository.create(booking)
                        .orElseThrow(() -> new DomainException("Не удалось сохранить бронирование", "BOOKING_CREATE_FAILED"));
                final ResourceBooking savedBooking = bookingRepository.findById(bookingId)
                        .orElseThrow(() -> new DomainException("Не удалось загрузить бронирование", "BOOKING_LOAD_FAILED"));
                createdBookings.add(savedBooking);
                pendingExtBookings.add(savedBooking);
                alreadyBookedResourceIds.add(resourceId);
            }

            return new Phase1Result(createdBookings, pendingExtBookings);
        });

        if (phase1 == null || phase1.allCreated().isEmpty()) {
            return new ReserveResourcesResponseDto(taskId, type, requiredCount, List.of());
        }

        if (phase1.pendingExternals().isEmpty()) {
            final List<ResourceBooking> finalBookings = new ArrayList<>(phase1.allCreated().size());
            for (final ResourceBooking b : phase1.allCreated()) {
                if (b.getId() != null) {
                    finalBookings.add(bookingRepository.findById(b.getId()).orElse(b));
                }
            }
            return new ReserveResourcesResponseDto(taskId, type, requiredCount, toResponseDtos(finalBookings));
        }

        final DateTimeRange asyncWindow = new DateTimeRange(reservedFrom, reservedTo);
        final ResourceType asyncType = type;
        final String asyncResourceName = resourceName;
        final List<Integer> pendingBookingIds = phase1.pendingExternals().stream()
                .map(ResourceBooking::getId)
                .filter(Objects::nonNull)
                .toList();

        CompletableFuture.runAsync(() -> {
            try {
                for (final Integer bookingId : pendingBookingIds) {
                    try {
                        final ExternalResource rawExternal = requestExternal(asyncType, asyncResourceName, asyncWindow);
                        transactionTemplate.executeWithoutResult(status -> {
                            if (rawExternal != null
                                    && rawExternal.getExternalApiId() != null
                                    && !rawExternal.getExternalApiId().isBlank()) {
                                confirmPendingBooking(bookingId, rawExternal.getExternalApiId());
                            } else {
                                failPendingBooking(bookingId, "Нет ресурсов у поставщика");
                            }
                        });
                    } catch (final Exception e) {
                        log.warn("Ошибка внешнего API для брони {}: {}", bookingId, e.getMessage());
                        transactionTemplate.executeWithoutResult(status ->
                                failPendingBooking(bookingId, "Сбой связи с поставщиком: " + e.getMessage())
                        );
                    }
                }
            } catch (final Throwable t) {
                log.error("Фоновое подтверждение внешних резервов завершилось с ошибкой", t);
            }
        });

        return new ReserveResourcesResponseDto(taskId, type, requiredCount, toResponseDtos(phase1.allCreated()));
    }

    private void confirmPendingBooking(final Integer bookingId, final String realApiId) {
        final ResourceBooking pendingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DomainException("Бронирование не найдено", "BOOKING_NOT_FOUND"));
        final Resource resource = pendingBooking.getResource();
        if (!(resource instanceof ExternalResource external)) {
            throw new DomainException("Ожидался внешний ресурс", "INVALID_RESOURCE_KIND");
        }
        external.updateExternalApiId(realApiId);
        externalResourceRepository.update(external);
        pendingBooking.confirm();
        bookingRepository.update(pendingBooking);
    }

    private void failPendingBooking(final Integer bookingId, final String reason) {
        final ResourceBooking pendingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DomainException("Бронирование не найдено", "BOOKING_NOT_FOUND"));
        pendingBooking.fail();
        bookingRepository.update(pendingBooking);
        log.info("Бронирование {} в статусе FAILED. Причина: {}", bookingId, reason);
    }

    private ExternalResource requestExternal(final ResourceType type, final String resourceName, final DateTimeRange window) {
        if (type == ResourceType.TRANSPORT) {
            return transportLogistics.requestTransport(resourceName, window);
        }
        return equipmentRental.request(type, resourceName, window);
    }

    private static boolean isUsable(final Resource resource, final Set<Integer> alreadyBookedResourceIds) {
        if (resource == null || resource.getId() == null) {
            return false;
        }
        if (!resource.isOperational()) {
            return false;
        }
        return !alreadyBookedResourceIds.contains(resource.getId());
    }

    private static Set<Integer> collectActiveBookedResourceIds(final List<ResourceBooking> activeForTask, final ResourceType type) {
        final Set<Integer> ids = new HashSet<>();
        for (final ResourceBooking b : activeForTask) {
            if (b == null || b.getResource() == null) {
                continue;
            }
            if (!isActive(b.getStatus())) {
                continue;
            }
            final Resource r = b.getResource();
            if (r.getType() != type) {
                continue;
            }
            if (r.getId() != null) {
                ids.add(r.getId());
            }
        }
        return ids;
    }

    private static boolean isActive(final BookingStatus status) {
        return status == BookingStatus.REQUESTED || status == BookingStatus.CONFIRMED;
    }

    private static List<ResourceBookingResponseDto> toResponseDtos(final List<ResourceBooking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return List.of();
        }
        final List<ResourceBookingResponseDto> dtos = new ArrayList<>(bookings.size());
        for (final ResourceBooking b : bookings) {
            if (b == null) {
                continue;
            }
            final ResourceBookingResponseDto dto = ResourceBookingResponseDto.from(b);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        return List.copyOf(dtos);
    }

    private static <T> List<T> safeList(final List<T> value) {
        return value == null ? List.of() : value;
    }
}
