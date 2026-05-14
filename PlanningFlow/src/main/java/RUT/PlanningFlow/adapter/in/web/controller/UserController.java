package RUT.PlanningFlow.adapter.in.web.controller;

import RUT.PlanningFlow.adapter.in.web.dto.user.AssignmentRejectRequest;
import RUT.PlanningFlow.adapter.in.web.dto.user.UserProfileUpdateRequest;
import RUT.PlanningFlow.adapter.in.web.dto.user.UserSkillsUpdateRequest;
import RUT.PlanningFlow.application.dto.user.UserResponseDto;
import RUT.PlanningFlow.application.dto.user.UserSkillResponseDto;
import RUT.PlanningFlow.application.dto.user.UserViewerContextDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.user.AcceptAssignmentUseCase;
import RUT.PlanningFlow.application.port.in.user.GetUserDetailsQuery;
import RUT.PlanningFlow.application.port.in.user.GetUserSkillsQuery;
import RUT.PlanningFlow.application.port.in.user.GetUserViewerContextQuery;
import RUT.PlanningFlow.application.port.in.user.ListUsersQuery;
import RUT.PlanningFlow.application.port.in.user.ManageUserSkillsUseCase;
import RUT.PlanningFlow.application.port.in.user.RejectAssignmentUseCase;
import RUT.PlanningFlow.application.port.in.user.UpdateProfileUseCase;
import RUT.PlanningFlow.adapter.in.web.security.JwtPrincipal;
import RUT.PlanningFlow.domain.enums.UserRoles;
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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@Validated
@Tag(name = "Пользователи", description = "Каталог, профиль, навыки, принятие/отказ по назначению")
public class UserController {

    private final ListUsersQuery listUsersQuery;
    private final GetUserDetailsQuery getUserDetailsQuery;
    private final GetUserSkillsQuery getUserSkillsQuery;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final ManageUserSkillsUseCase manageUserSkillsUseCase;
    private final AcceptAssignmentUseCase acceptAssignmentUseCase;
    private final RejectAssignmentUseCase rejectAssignmentUseCase;
    private final GetUserViewerContextQuery getUserViewerContextQuery;

    public UserController(
            final ListUsersQuery listUsersQuery,
            final GetUserDetailsQuery getUserDetailsQuery,
            final GetUserSkillsQuery getUserSkillsQuery,
            final UpdateProfileUseCase updateProfileUseCase,
            final ManageUserSkillsUseCase manageUserSkillsUseCase,
            final AcceptAssignmentUseCase acceptAssignmentUseCase,
            final RejectAssignmentUseCase rejectAssignmentUseCase,
            final GetUserViewerContextQuery getUserViewerContextQuery
    ) {
        this.listUsersQuery = listUsersQuery;
        this.getUserDetailsQuery = getUserDetailsQuery;
        this.getUserSkillsQuery = getUserSkillsQuery;
        this.updateProfileUseCase = updateProfileUseCase;
        this.manageUserSkillsUseCase = manageUserSkillsUseCase;
        this.acceptAssignmentUseCase = acceptAssignmentUseCase;
        this.rejectAssignmentUseCase = rejectAssignmentUseCase;
        this.getUserViewerContextQuery = getUserViewerContextQuery;
    }

    @GetMapping
    @Operation(summary = "Список пользователей", description = "Поиск по имени и роли")
    public ResponseEntity<PageResult<UserResponseDto>> list(
            final Authentication authentication,
            @RequestParam(required = false) final String username,
            @RequestParam(required = false) final String role,
            @RequestParam(defaultValue = "1") @Min(1) final int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(500) final int size
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        final PageQuery pageQuery = new PageQuery(page, size);
        final UserRoles roleFilter = parseRoleFilter(role);
        final PageResult<UserResponseDto> result =
                listUsersQuery.execute(principal.userId(), username, roleFilter, pageQuery);
        return ResponseEntity.ok(result);
    }

    private static UserRoles parseRoleFilter(final String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UserRoles.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неизвестная роль");
        }
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Карточка пользователя")
    public ResponseEntity<UserResponseDto> getById(@PathVariable final Integer userId) {
        final Optional<UserResponseDto> dto = getUserDetailsQuery.execute(userId);
        return dto.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    @GetMapping("/{userId}/skills")
    @Operation(summary = "Навыки пользователя")
    public ResponseEntity<List<UserSkillResponseDto>> listSkills(@PathVariable final Integer userId) {
        final List<UserSkillResponseDto> skills = getUserSkillsQuery.execute(userId);
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/{userId}/viewer-context")
    @Operation(summary = "Контекст просмотра профиля", description = "Роли и видимость для текущего JWT")
    public ResponseEntity<UserViewerContextDto> viewerContext(
            @PathVariable final Integer userId,
            final Authentication authentication
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        final UserViewerContextDto dto = getUserViewerContextQuery.execute(principal.userId(), userId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{userId}/profile")
    @Operation(summary = "Обновить ФИО профиля")
    public ResponseEntity<Integer> updateProfile(
            @PathVariable final Integer userId,
            @Valid @RequestBody final UserProfileUpdateRequest request
    ) {
        final String fullName = blankToNull(request.getFullName());
        final Optional<Integer> updatedId = updateProfileUseCase.execute(userId, fullName);
        return updatedId.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    @PutMapping("/{userId}/skills")
    @Operation(summary = "Заменить набор навыков пользователя")
    public ResponseEntity<List<Integer>> manageSkills(
            @PathVariable final Integer userId,
            @Valid @RequestBody final UserSkillsUpdateRequest request
    ) {
        final List<Integer> createdIds = manageUserSkillsUseCase.execute(userId, request.toTierMap());
        return ResponseEntity.ok(createdIds);
    }

    @PostMapping("/assignments/{assignmentId}/accept")
    @Operation(summary = "Принять назначение на задачу")
    public ResponseEntity<Void> acceptAssignment(@PathVariable final Integer assignmentId) {
        acceptAssignmentUseCase.execute(assignmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assignments/{assignmentId}/reject")
    @Operation(summary = "Отклонить назначение", description = "С причиной")
    public ResponseEntity<Void> rejectAssignment(
            @PathVariable final Integer assignmentId,
            @Valid @RequestBody final AssignmentRejectRequest request
    ) {
        rejectAssignmentUseCase.execute(assignmentId, request.getReason());
        return ResponseEntity.noContent().build();
    }

    private static String blankToNull(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}