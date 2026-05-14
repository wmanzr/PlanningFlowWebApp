package RUT.PlanningFlow.adapter.in.web.controller;

import RUT.PlanningFlow.adapter.in.web.dto.resource.InternalResourceCreateRequest;
import RUT.PlanningFlow.adapter.in.web.dto.resource.InternalResourceUpdateRequest;
import RUT.PlanningFlow.application.dto.resource.ExternalResourceResponseDto;
import RUT.PlanningFlow.application.dto.resource.InternalResourceResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.resource.CreateInternalResourceUseCase;
import RUT.PlanningFlow.application.port.in.resource.DeleteInternalResourceUseCase;
import RUT.PlanningFlow.application.port.in.resource.GetResourceDetailsQuery;
import RUT.PlanningFlow.application.port.in.resource.ListResourcesQuery;
import RUT.PlanningFlow.application.port.in.resource.MarkResourceBrokenUseCase;
import RUT.PlanningFlow.application.port.in.resource.MarkResourceOperationalUseCase;
import RUT.PlanningFlow.application.port.in.resource.UpdateInternalResourceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/resources")
@Validated
@Tag(name = "Ресурсы", description = "Внутренние ресурсы (инвентарь) и карточка внешнего ресурса")
public class ResourceController {

    private final CreateInternalResourceUseCase createInternalResourceUseCase;
    private final UpdateInternalResourceUseCase updateInternalResourceUseCase;
    private final GetResourceDetailsQuery getResourceDetailsQuery;
    private final ListResourcesQuery listResourcesQuery;
    private final MarkResourceOperationalUseCase markResourceOperationalUseCase;
    private final MarkResourceBrokenUseCase markResourceBrokenUseCase;
    private final DeleteInternalResourceUseCase deleteInternalResourceUseCase;

    public ResourceController(
            final CreateInternalResourceUseCase createInternalResourceUseCase,
            final UpdateInternalResourceUseCase updateInternalResourceUseCase,
            final GetResourceDetailsQuery getResourceDetailsQuery,
            final ListResourcesQuery listResourcesQuery,
            final MarkResourceOperationalUseCase markResourceOperationalUseCase,
            final MarkResourceBrokenUseCase markResourceBrokenUseCase,
            final DeleteInternalResourceUseCase deleteInternalResourceUseCase
    ) {
        this.createInternalResourceUseCase = createInternalResourceUseCase;
        this.updateInternalResourceUseCase = updateInternalResourceUseCase;
        this.getResourceDetailsQuery = getResourceDetailsQuery;
        this.listResourcesQuery = listResourcesQuery;
        this.markResourceOperationalUseCase = markResourceOperationalUseCase;
        this.markResourceBrokenUseCase = markResourceBrokenUseCase;
        this.deleteInternalResourceUseCase = deleteInternalResourceUseCase;
    }

    @GetMapping("/internal")
    @Operation(summary = "Список внутренних ресурсов")
    public ResponseEntity<PageResult<InternalResourceResponseDto>> listInternal(
            @RequestParam(required = false) final String name,
            @RequestParam(defaultValue = "1") @Min(1) final int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(500) final int size
    ) {
        final String searchName = blankToNull(name);
        final PageQuery pageQuery = new PageQuery(page, size);
        final PageResult<InternalResourceResponseDto> result = listResourcesQuery.execute(searchName, pageQuery);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/internal")
    @Operation(summary = "Создать внутренний ресурс")
    public ResponseEntity<Integer> createInternal(@Valid @RequestBody final InternalResourceCreateRequest request) {
        final Integer resourceId = createInternalResourceUseCase.execute(
                request.getName(),
                request.getType(),
                request.getInventoryNumber()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceId);
    }

    @GetMapping("/internal/{resourceId}")
    @Operation(summary = "Карточка внутреннего ресурса")
    public ResponseEntity<InternalResourceResponseDto> getInternal(@PathVariable final Integer resourceId) {
        return optionalInternal(getResourceDetailsQuery.execute(resourceId));
    }

    @PutMapping("/internal/{resourceId}")
    @Operation(summary = "Обновить внутренний ресурс")
    public ResponseEntity<Integer> updateInternal(
            @PathVariable final Integer resourceId,
            @Valid @RequestBody final InternalResourceUpdateRequest request
    ) {
        final Optional<Integer> updatedId = updateInternalResourceUseCase.execute(
                resourceId,
                blankToNull(request.getName()),
                request.getType(),
                blankToNull(request.getInventoryNumber())
        );
        return updatedId.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ресурс не найден"));
    }

    @DeleteMapping("/internal/{resourceId}")
    @Operation(summary = "Удалить внутренний ресурс")
    public ResponseEntity<Void> deleteInternal(@PathVariable final Integer resourceId) {
        if (!deleteInternalResourceUseCase.execute(resourceId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ресурс не найден");
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/internal/{resourceId}/status/operational")
    @Operation(summary = "Статус: исправен / в строю")
    public ResponseEntity<Integer> markOperational(@PathVariable final Integer resourceId) {
        return markResourceOperationalUseCase.execute(resourceId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ресурс не найден"));
    }

    @PostMapping("/internal/{resourceId}/status/broken")
    @Operation(summary = "Статус: неисправен")
    public ResponseEntity<Integer> markBroken(@PathVariable final Integer resourceId) {
        return markResourceBrokenUseCase.execute(resourceId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ресурс не найден"));
    }

    @GetMapping("/external/{resourceId}")
    @Operation(summary = "Карточка внешнего ресурса")
    public ResponseEntity<ExternalResourceResponseDto> getExternal(@PathVariable final Integer resourceId) {
        return getResourceDetailsQuery.executeExternal(resourceId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ресурс не найден"));
    }

    private static String blankToNull(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static ResponseEntity<InternalResourceResponseDto> optionalInternal(
            final Optional<InternalResourceResponseDto> dto
    ) {
        return dto.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ресурс не найден"));
    }
}
