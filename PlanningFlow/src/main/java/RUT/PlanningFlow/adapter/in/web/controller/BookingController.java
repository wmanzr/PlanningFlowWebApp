package RUT.PlanningFlow.adapter.in.web.controller;

import RUT.PlanningFlow.adapter.in.web.dto.resource.ResourceBookingRescheduleRequest;
import RUT.PlanningFlow.application.dto.resource.ResourceBookingResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.booking.GetResourceBookingDetailsQuery;
import RUT.PlanningFlow.application.port.in.booking.ListResourceBookingsForTaskQuery;
import RUT.PlanningFlow.application.port.in.booking.ManageResourceBookingStatusUseCase;
import RUT.PlanningFlow.application.port.in.booking.RescheduleResourceBookingUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/bookings")
@Validated
@Tag(name = "Бронирования", description = "Брони ресурсов по задаче: просмотр, перенос, статусы")
public class BookingController {

    private final GetResourceBookingDetailsQuery getResourceBookingDetailsQuery;
    private final ListResourceBookingsForTaskQuery listResourceBookingsForTaskQuery;
    private final ManageResourceBookingStatusUseCase manageResourceBookingStatusUseCase;
    private final RescheduleResourceBookingUseCase rescheduleResourceBookingUseCase;

    public BookingController(
            final GetResourceBookingDetailsQuery getResourceBookingDetailsQuery,
            final ListResourceBookingsForTaskQuery listResourceBookingsForTaskQuery,
            final ManageResourceBookingStatusUseCase manageResourceBookingStatusUseCase,
            final RescheduleResourceBookingUseCase rescheduleResourceBookingUseCase
    ) {
        this.getResourceBookingDetailsQuery = getResourceBookingDetailsQuery;
        this.listResourceBookingsForTaskQuery = listResourceBookingsForTaskQuery;
        this.manageResourceBookingStatusUseCase = manageResourceBookingStatusUseCase;
        this.rescheduleResourceBookingUseCase = rescheduleResourceBookingUseCase;
    }

    @GetMapping("/for-task/{taskId}")
    @Operation(summary = "Брони по задаче")
    public ResponseEntity<PageResult<ResourceBookingResponseDto>> listForTask(
            @PathVariable final Integer taskId,
            @RequestParam(defaultValue = "1") @Min(1) final int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(500) final int size
    ) {
        final PageQuery pageQuery = new PageQuery(page, size);
        final PageResult<ResourceBookingResponseDto> result = listResourceBookingsForTaskQuery.execute(taskId, pageQuery);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Карточка брони")
    public ResponseEntity<ResourceBookingResponseDto> getById(@PathVariable final Integer bookingId) {
        final Optional<ResourceBookingResponseDto> dto = getResourceBookingDetailsQuery.execute(bookingId);
        return dto.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
    }

    @PutMapping("/{bookingId}/schedule")
    @Operation(summary = "Перенести окно брони")
    public ResponseEntity<Integer> reschedule(
            @PathVariable final Integer bookingId,
            @Valid @RequestBody final ResourceBookingRescheduleRequest request
    ) {
        final Optional<Integer> updatedId = rescheduleResourceBookingUseCase.execute(
                bookingId,
                request.getReservedFrom(),
                request.getReservedTo()
        );
        return optionalBookingId(updatedId);
    }

    @PostMapping("/{bookingId}/status/confirm")
    @Operation(summary = "Подтвердить бронь")
    public ResponseEntity<Integer> confirm(@PathVariable final Integer bookingId) {
        return optionalBookingId(manageResourceBookingStatusUseCase.confirm(bookingId));
    }

    @PostMapping("/{bookingId}/status/fail")
    @Operation(summary = "Пометить бронь как сорванную")
    public ResponseEntity<Integer> fail(@PathVariable final Integer bookingId) {
        return optionalBookingId(manageResourceBookingStatusUseCase.fail(bookingId));
    }

    @PostMapping("/{bookingId}/status/cancel")
    @Operation(summary = "Отменить бронь")
    public ResponseEntity<Integer> cancel(@PathVariable final Integer bookingId) {
        return optionalBookingId(manageResourceBookingStatusUseCase.cancel(bookingId));
    }

    private static ResponseEntity<Integer> optionalBookingId(final Optional<Integer> id) {
        return id.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
    }
}