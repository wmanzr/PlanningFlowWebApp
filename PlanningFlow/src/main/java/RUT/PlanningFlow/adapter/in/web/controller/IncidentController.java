package RUT.PlanningFlow.adapter.in.web.controller;

import RUT.PlanningFlow.adapter.in.web.dto.incident.IncidentCreateRequest;
import RUT.PlanningFlow.adapter.in.web.dto.incident.IncidentResolveRequest;
import RUT.PlanningFlow.application.dto.incident.IncidentResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.incident.AcceptIncidentForHandlingUseCase;
import RUT.PlanningFlow.application.port.in.incident.GetIncidentDetailsQuery;
import RUT.PlanningFlow.application.port.in.incident.GetIncidentsForEventQuery;
import RUT.PlanningFlow.application.port.in.incident.ReportIncidentUseCase;
import RUT.PlanningFlow.application.port.in.incident.ResolveIncidentUseCase;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/incidents")
@Validated
@Tag(name = "Инциденты", description = "Реестр, создание, принятие в работу, закрытие")
public class IncidentController {

    private final ReportIncidentUseCase reportIncidentUseCase;
    private final GetIncidentsForEventQuery getIncidentsForEventQuery;
    private final GetIncidentDetailsQuery getIncidentDetailsQuery;
    private final AcceptIncidentForHandlingUseCase acceptIncidentForHandlingUseCase;
    private final ResolveIncidentUseCase resolveIncidentUseCase;

    public IncidentController(
            final ReportIncidentUseCase reportIncidentUseCase,
            final GetIncidentsForEventQuery getIncidentsForEventQuery,
            final GetIncidentDetailsQuery getIncidentDetailsQuery,
            final AcceptIncidentForHandlingUseCase acceptIncidentForHandlingUseCase,
            final ResolveIncidentUseCase resolveIncidentUseCase
    ) {
        this.reportIncidentUseCase = reportIncidentUseCase;
        this.getIncidentsForEventQuery = getIncidentsForEventQuery;
        this.getIncidentDetailsQuery = getIncidentDetailsQuery;
        this.acceptIncidentForHandlingUseCase = acceptIncidentForHandlingUseCase;
        this.resolveIncidentUseCase = resolveIncidentUseCase;
    }

    @GetMapping("/for-event/{eventId}")
    @Operation(summary = "Инциденты мероприятия")
    public ResponseEntity<PageResult<IncidentResponseDto>> listForEvent(
            @PathVariable final Integer eventId,
            @RequestParam(defaultValue = "1") @Min(1) final int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(500) final int size
    ) {
        final PageQuery pageQuery = new PageQuery(page, size);
        final PageResult<IncidentResponseDto> result = getIncidentsForEventQuery.execute(eventId, pageQuery);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{incidentId}")
    @Operation(summary = "Карточка инцидента")
    public ResponseEntity<IncidentResponseDto> getById(@PathVariable final Integer incidentId) {
        final Optional<IncidentResponseDto> dto = getIncidentDetailsQuery.execute(incidentId);
        return dto.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Инцидент не найден"));
    }

    @PostMapping
    @Operation(summary = "Создать инцидент", description = "Репорт с JWT")
    public ResponseEntity<Integer> report(@Valid @RequestBody final IncidentCreateRequest request) {
        final Integer incidentId = reportIncidentUseCase.execute(
                request.getReporterId(),
                request.getEventId(),
                request.getTaskId(),
                request.getResourceId(),
                request.getDescription(),
                request.getSeverity()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentId);
    }

    @PostMapping("/{incidentId}/accept")
    @Operation(summary = "Принять инцидент в работу")
    public ResponseEntity<Void> accept(@PathVariable final Integer incidentId) {
        acceptIncidentForHandlingUseCase.execute(incidentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{incidentId}/resolve")
    @Operation(summary = "Закрыть инцидент", description = "С итоговыми заметками")
    public ResponseEntity<Void> resolve(
            @PathVariable final Integer incidentId,
            @Valid @RequestBody final IncidentResolveRequest request
    ) {
        resolveIncidentUseCase.execute(incidentId, request.getResolutionNotes());
        return ResponseEntity.noContent().build();
    }
}
