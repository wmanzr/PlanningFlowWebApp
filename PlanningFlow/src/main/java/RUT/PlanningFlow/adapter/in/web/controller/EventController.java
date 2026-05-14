package RUT.PlanningFlow.adapter.in.web.controller;

import RUT.PlanningFlow.adapter.in.web.dto.event.EventCancelRequest;
import RUT.PlanningFlow.adapter.in.web.dto.event.EventCreateRequest;
import RUT.PlanningFlow.adapter.in.web.dto.event.EventUpdateRequest;
import RUT.PlanningFlow.application.dto.event.EventDashboardResponseDto;
import RUT.PlanningFlow.application.dto.event.EventPostMortemAiReportResponseDto;
import RUT.PlanningFlow.application.dto.event.EventResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.event.CreateEventUseCase;
import RUT.PlanningFlow.application.port.in.event.GeneratePostMortemAIReportUseCase;
import RUT.PlanningFlow.application.port.in.event.GetEventDashboardQuery;
import RUT.PlanningFlow.application.port.in.event.GetEventDetailsQuery;
import RUT.PlanningFlow.application.port.in.event.GetEventPostMortemAiReportQuery;
import RUT.PlanningFlow.application.port.in.event.ListEventsByDateRangeQuery;
import RUT.PlanningFlow.application.port.in.event.ManageEventStatusUseCase;
import RUT.PlanningFlow.application.port.in.event.UpdateEventUseCase;
import RUT.PlanningFlow.adapter.in.web.security.JwtPrincipal;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/events")
@Validated
@Tag(name = "Мероприятия", description = "Мероприятия: статусы жизненного цикла, дашборд и отчет ИИ после завершения")
public class EventController {

    private final CreateEventUseCase createEventUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final GetEventDetailsQuery getEventDetailsQuery;
    private final ListEventsByDateRangeQuery listEventsByDateRangeQuery;
    private final GetEventDashboardQuery getEventDashboardQuery;
    private final ManageEventStatusUseCase manageEventStatusUseCase;
    private final GeneratePostMortemAIReportUseCase generatePostMortemAIReportUseCase;
    private final GetEventPostMortemAiReportQuery getEventPostMortemAiReportQuery;

    public EventController(
            final CreateEventUseCase createEventUseCase,
            final UpdateEventUseCase updateEventUseCase,
            final GetEventDetailsQuery getEventDetailsQuery,
            final ListEventsByDateRangeQuery listEventsByDateRangeQuery,
            final GetEventDashboardQuery getEventDashboardQuery,
            final ManageEventStatusUseCase manageEventStatusUseCase,
            final GeneratePostMortemAIReportUseCase generatePostMortemAIReportUseCase,
            final GetEventPostMortemAiReportQuery getEventPostMortemAiReportQuery
    ) {
        this.createEventUseCase = createEventUseCase;
        this.updateEventUseCase = updateEventUseCase;
        this.getEventDetailsQuery = getEventDetailsQuery;
        this.listEventsByDateRangeQuery = listEventsByDateRangeQuery;
        this.getEventDashboardQuery = getEventDashboardQuery;
        this.manageEventStatusUseCase = manageEventStatusUseCase;
        this.generatePostMortemAIReportUseCase = generatePostMortemAIReportUseCase;
        this.getEventPostMortemAiReportQuery = getEventPostMortemAiReportQuery;
    }

    @GetMapping
    @Operation(summary = "Список мероприятий", description = "По датам и названию, с пагинацией")
    public ResponseEntity<PageResult<EventResponseDto>> list(
            final Authentication authentication,
            @RequestParam(required = false) final LocalDateTime start,
            @RequestParam(required = false) final LocalDateTime end,
            @RequestParam(required = false) final String title,
            @RequestParam(defaultValue = "1") @Min(1) final int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(500) final int size
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        final PageQuery pageQuery = new PageQuery(page, size);
        final PageResult<EventResponseDto> result =
                listEventsByDateRangeQuery.execute(principal.userId(), start, end, title, pageQuery);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Карточка мероприятия")
    public ResponseEntity<EventResponseDto> getById(
            final Authentication authentication,
            @PathVariable final Integer eventId
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        final Optional<EventResponseDto> dto = getEventDetailsQuery.execute(principal.userId(), eventId);
        return dto.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Мероприятие не найдено"));
    }

    @GetMapping("/{eventId}/dashboard")
    @Operation(summary = "Дашборд по мероприятию", description = "Сводка задач и инцидентов")
    public ResponseEntity<EventDashboardResponseDto> getDashboard(
            final Authentication authentication,
            @PathVariable final Integer eventId
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        final EventDashboardResponseDto dashboard = getEventDashboardQuery.execute(principal.userId(), eventId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/{eventId}/post-mortem/ai")
    @Operation(summary = "Отчет ИИ (чтение)", description = "Статус и текст рекомендаций для завершенного мероприятия")
    public ResponseEntity<EventPostMortemAiReportResponseDto> getPostMortemAiReport(
            final Authentication authentication,
            @PathVariable final Integer eventId
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return getEventPostMortemAiReportQuery.execute(principal.userId(), eventId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/{eventId}/status/start-planning")
    @Operation(summary = "Статус: планирование")
    public ResponseEntity<Integer> startPlanning(
            final Authentication authentication,
            @PathVariable final Integer eventId
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return statusIdResponse(manageEventStatusUseCase.startPlanning(principal.userId(), eventId));
    }

    @PostMapping("/{eventId}/status/activate")
    @Operation(summary = "Статус: активно")
    public ResponseEntity<Integer> activate(
            final Authentication authentication,
            @PathVariable final Integer eventId
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return statusIdResponse(manageEventStatusUseCase.activate(principal.userId(), eventId));
    }

    @PostMapping("/{eventId}/status/complete")
    @Operation(summary = "Завершить мероприятие")
    public ResponseEntity<Integer> complete(
            final Authentication authentication,
            @PathVariable final Integer eventId
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return statusIdResponse(manageEventStatusUseCase.complete(principal.userId(), eventId));
    }

    @PostMapping("/{eventId}/status/cancel")
    @Operation(summary = "Отменить мероприятие", description = "Только организатор/админ")
    public ResponseEntity<Integer> cancel(
            final Authentication authentication,
            @PathVariable final Integer eventId,
            @Valid @RequestBody final EventCancelRequest request
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return statusIdResponse(manageEventStatusUseCase.cancel(principal.userId(), eventId, request.getReason()));
    }

    @PostMapping("/{eventId}/post-mortem/ai")
    @Operation(summary = "Перезапуск отчета ИИ", description = "Фоновая генерация для завершенного мероприятия")
    public ResponseEntity<Void> generatePostMortemAiReport(
            final Authentication authentication,
            @PathVariable final Integer eventId
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        generatePostMortemAIReportUseCase.execute(principal.userId(), eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @Operation(summary = "Создать мероприятие")
    public ResponseEntity<Integer> create(
            final Authentication authentication,
            @Valid @RequestBody final EventCreateRequest request
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        final GeoPoint location = toGeoPointOrNull(request.getLatitude(), request.getLongitude());
        final Integer eventId = createEventUseCase.execute(
                request.getTitle(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                location,
                principal.userId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(eventId);
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "Обновить мероприятие", description = "Поля и координаторы")
    public ResponseEntity<Integer> update(
            final Authentication authentication,
            @PathVariable final Integer eventId,
            @Valid @RequestBody final EventUpdateRequest request
    ) {
        if (!eventId.equals(request.getEventId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Идентификатор в пути и в теле запроса должны совпадать");
        }

        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();

        final GeoPoint newLocation;
        if (request.getLatitude() == null && request.getLongitude() == null) {
            newLocation = null;
        } else {
            newLocation = new GeoPoint(request.getLatitude(), request.getLongitude());
        }

        final Optional<Integer> updatedId = updateEventUseCase.execute(
                principal.userId(),
                request.getEventId(),
                request.getTitle(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                newLocation,
                request.getCoordinatorIds()
        );

        if (updatedId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Мероприятие не найдено");
        }
        return ResponseEntity.ok(updatedId.get());
    }

    private static GeoPoint toGeoPointOrNull(final Double latitude, final Double longitude) {
        if (latitude == null && longitude == null) {
            return null;
        }
        return new GeoPoint(latitude, longitude);
    }

    private static ResponseEntity<Integer> statusIdResponse(final Optional<Integer> updatedId) {
        return updatedId.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Мероприятие не найдено"));
    }
}
