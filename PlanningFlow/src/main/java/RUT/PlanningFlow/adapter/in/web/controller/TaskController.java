package RUT.PlanningFlow.adapter.in.web.controller;

import RUT.PlanningFlow.adapter.in.web.dto.resource.TaskAllocateResourcesRequest;
import RUT.PlanningFlow.adapter.in.web.dto.task.TaskAssignRequest;
import RUT.PlanningFlow.adapter.in.web.dto.task.TaskCreateRequest;
import RUT.PlanningFlow.adapter.in.web.dto.task.TaskMatchRequest;
import RUT.PlanningFlow.adapter.in.web.dto.task.TaskUpdateRequest;
import RUT.PlanningFlow.application.dto.matching.MatchTaskResponseDto;
import RUT.PlanningFlow.application.dto.resource.ReserveResourcesResponseDto;
import RUT.PlanningFlow.application.dto.task.TaskResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.matching.MatchTaskUseCase;
import RUT.PlanningFlow.application.port.in.task.AllocateTaskResourcesUseCase;
import RUT.PlanningFlow.application.port.in.task.AssignParticipantUseCase;
import RUT.PlanningFlow.application.port.in.task.CreateTaskUseCase;
import RUT.PlanningFlow.application.port.in.task.GetMyTasksQuery;
import RUT.PlanningFlow.application.port.in.task.GetTaskDetailsQuery;
import RUT.PlanningFlow.application.port.in.task.ListTasksForEventQuery;
import RUT.PlanningFlow.application.port.in.task.ManageTaskStatusUseCase;
import RUT.PlanningFlow.application.port.in.task.UnassignParticipantUseCase;
import RUT.PlanningFlow.application.port.in.task.UpdateTaskUseCase;
import RUT.PlanningFlow.adapter.in.web.security.JwtPrincipal;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tasks")
@Validated
@Tag(name = "Задачи", description = "CRUD, статусы, назначения, подбор исполнителей, бронирование ресурсов")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final GetTaskDetailsQuery getTaskDetailsQuery;
    private final ListTasksForEventQuery listTasksForEventQuery;
    private final GetMyTasksQuery getMyTasksQuery;
    private final ManageTaskStatusUseCase manageTaskStatusUseCase;
    private final AssignParticipantUseCase assignParticipantUseCase;
    private final UnassignParticipantUseCase unassignParticipantUseCase;
    private final AllocateTaskResourcesUseCase allocateTaskResourcesUseCase;
    private final MatchTaskUseCase matchTaskUseCase;

    public TaskController(
            final CreateTaskUseCase createTaskUseCase,
            final UpdateTaskUseCase updateTaskUseCase,
            final GetTaskDetailsQuery getTaskDetailsQuery,
            final ListTasksForEventQuery listTasksForEventQuery,
            final GetMyTasksQuery getMyTasksQuery,
            final ManageTaskStatusUseCase manageTaskStatusUseCase,
            final AssignParticipantUseCase assignParticipantUseCase,
            final UnassignParticipantUseCase unassignParticipantUseCase,
            final AllocateTaskResourcesUseCase allocateTaskResourcesUseCase,
            final MatchTaskUseCase matchTaskUseCase
    ) {
        this.createTaskUseCase = createTaskUseCase;
        this.updateTaskUseCase = updateTaskUseCase;
        this.getTaskDetailsQuery = getTaskDetailsQuery;
        this.listTasksForEventQuery = listTasksForEventQuery;
        this.getMyTasksQuery = getMyTasksQuery;
        this.manageTaskStatusUseCase = manageTaskStatusUseCase;
        this.assignParticipantUseCase = assignParticipantUseCase;
        this.unassignParticipantUseCase = unassignParticipantUseCase;
        this.allocateTaskResourcesUseCase = allocateTaskResourcesUseCase;
        this.matchTaskUseCase = matchTaskUseCase;
    }

    @GetMapping("/for-event/{eventId}")
    @Operation(summary = "Задачи мероприятия", description = "Страница по eventId")
    public ResponseEntity<PageResult<TaskResponseDto>> listForEvent(
            final Authentication authentication,
            @PathVariable final Integer eventId,
            @RequestParam(required = false) final LocalDateTime start,
            @RequestParam(required = false) final LocalDateTime end,
            @RequestParam(defaultValue = "1") @Min(1) final int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(500) final int size
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        final PageQuery pageQuery = new PageQuery(page, size);
        final PageResult<TaskResponseDto> result =
                listTasksForEventQuery.execute(principal.userId(), eventId, start, end, pageQuery);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/for-user/{userId}")
    @Operation(summary = "Задачи пользователя", description = "Фильтр по назначениям; не-админ видит только себя")
    public ResponseEntity<PageResult<TaskResponseDto>> listForUser(
            final Authentication authentication,
            @PathVariable final Integer userId,
            @RequestParam @NotNull final GetMyTasksQuery.AssignmentFilter filter,
            @RequestParam(required = false) final LocalDateTime start,
            @RequestParam(required = false) final LocalDateTime end,
            @RequestParam(required = false) final String title,
            @RequestParam(defaultValue = "1") @Min(1) final int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(500) final int size
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        final PageQuery pageQuery = new PageQuery(page, size);
        
        final int listUserId = isAdmin(authentication) ? userId : principal.userId();
        final PageResult<TaskResponseDto> result =
                getMyTasksQuery.execute(principal.userId(), listUserId, filter, start, end, title, pageQuery);
        return ResponseEntity.ok(result);
    }

    private static boolean isAdmin(final Authentication authentication) {
        for (final GrantedAuthority a : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Карточка задачи")
    public ResponseEntity<TaskResponseDto> getById(
            final Authentication authentication,
            @PathVariable final Integer taskId
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        final Optional<TaskResponseDto> dto = getTaskDetailsQuery.execute(principal.userId(), taskId);
        return dto.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));
    }

    @PostMapping
    @Operation(summary = "Создать задачу")
    public ResponseEntity<Integer> create(
            final Authentication authentication,
            @Valid @RequestBody final TaskCreateRequest request
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        final GeoPoint location = toGeoPointOrNull(request.getLatitude(), request.getLongitude());
        final Integer taskId = createTaskUseCase.execute(
                principal.userId(),
                request.getEventId(),
                request.getTitle(),
                request.getStartTime(),
                request.getEndTime(),
                location,
                request.getRequiredSkillIds()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(taskId);
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Обновить задачу")
    public ResponseEntity<Integer> update(
            final Authentication authentication,
            @PathVariable final Integer taskId,
            @Valid @RequestBody final TaskUpdateRequest request
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        final GeoPoint newLocation = toGeoPointOrNull(request.getLatitude(), request.getLongitude());
        final String newTitle = blankToNull(request.getNewTitle());
        final Optional<Integer> updatedId = updateTaskUseCase.execute(
                principal.userId(),
                taskId,
                newTitle,
                request.getNewStartTime(),
                request.getNewEndTime(),
                newLocation,
                request.getRequiredSkillIds(),
                request.getDependencyIds()
        );

        if (updatedId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена");
        }
        return ResponseEntity.ok(updatedId.get());
    }

    @PostMapping("/{taskId}/status/start-execution")
    @Operation(summary = "Статус: в работе", description = "Исполнитель")
    public ResponseEntity<Integer> startExecution(
            final Authentication authentication,
            @PathVariable final Integer taskId
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return taskStatusIdResponse(manageTaskStatusUseCase.startExecution(principal.userId(), taskId));
    }

    @PostMapping("/{taskId}/status/done")
    @Operation(summary = "Статус: выполнено", description = "Исполнитель")
    public ResponseEntity<Integer> markDone(
            final Authentication authentication,
            @PathVariable final Integer taskId
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return taskStatusIdResponse(manageTaskStatusUseCase.markAsDone(principal.userId(), taskId));
    }

    @PostMapping("/{taskId}/status/cancel")
    @Operation(summary = "Статус: отмена задачи")
    public ResponseEntity<Integer> cancel(
            final Authentication authentication,
            @PathVariable final Integer taskId
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return taskStatusIdResponse(manageTaskStatusUseCase.cancel(principal.userId(), taskId));
    }

    @PostMapping("/{taskId}/assignments")
    @Operation(summary = "Назначить исполнителя")
    public ResponseEntity<Integer> assign(
            final Authentication authentication,
            @PathVariable final Integer taskId,
            @Valid @RequestBody final TaskAssignRequest request
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        final Integer assignmentId = assignParticipantUseCase.execute(principal.userId(), taskId, request.getUserId());
        return ResponseEntity.ok(assignmentId);
    }

    @DeleteMapping("/{taskId}/assignments/{userId}")
    @Operation(summary = "Снять исполнителя")
    public ResponseEntity<Void> unassign(
            final Authentication authentication,
            @PathVariable final Integer taskId,
            @PathVariable final Integer userId
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        unassignParticipantUseCase.execute(principal.userId(), taskId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/matching")
    @Operation(summary = "Подбор кандидатов", description = "По навыкам и ограничениям")
    public ResponseEntity<MatchTaskResponseDto> matchParticipants(
            final Authentication authentication,
            @PathVariable final Integer taskId,
            @Valid @RequestBody final TaskMatchRequest request
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        final MatchTaskResponseDto result = matchTaskUseCase.execute(
                principal.userId(),
                taskId,
                request.toEventMode(),
                request.getRequiredCount()
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{taskId}/resources/allocate")
    @Operation(summary = "Зарезервировать ресурсы под задачу")
    public ResponseEntity<ReserveResourcesResponseDto> allocateResources(
            final Authentication authentication,
            @PathVariable final Integer taskId,
            @Valid @RequestBody final TaskAllocateResourcesRequest request
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        final ReserveResourcesResponseDto result = allocateTaskResourcesUseCase.execute(
                principal.userId(),
                taskId,
                request.getResourceType(),
                request.getResourceName(),
                request.getRequiredCount(),
                request.getReservedFrom(),
                request.getReservedTo()
        );
        return ResponseEntity.ok(result);
    }

    private static GeoPoint toGeoPointOrNull(final Double latitude, final Double longitude) {
        if (latitude == null && longitude == null) {
            return null;
        }
        return new GeoPoint(latitude, longitude);
    }

    private static String blankToNull(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static ResponseEntity<Integer> taskStatusIdResponse(final Optional<Integer> updatedId) {
        return updatedId.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));
    }
}
