package RUT.PlanningFlow.adapter.in.web.controller;

import RUT.PlanningFlow.application.dto.notification.NotificationDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.notification.ListNotificationsQuery;
import RUT.PlanningFlow.application.port.in.notification.MarkAllNotificationsReadUseCase;
import RUT.PlanningFlow.application.port.in.notification.MarkNotificationReadUseCase;
import RUT.PlanningFlow.application.port.in.notification.UnreadNotificationsCountQuery;
import RUT.PlanningFlow.adapter.in.web.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated
@Tag(name = "Уведомления", description = "Лента, непрочитанные, отметка прочитанным")
public class NotificationController {

    private final ListNotificationsQuery listNotificationsQuery;
    private final UnreadNotificationsCountQuery unreadCountQuery;
    private final MarkNotificationReadUseCase markReadUseCase;
    private final MarkAllNotificationsReadUseCase markAllReadUseCase;

    public NotificationController(
            final ListNotificationsQuery listNotificationsQuery,
            final UnreadNotificationsCountQuery unreadCountQuery,
            final MarkNotificationReadUseCase markReadUseCase,
            final MarkAllNotificationsReadUseCase markAllReadUseCase
    ) {
        this.listNotificationsQuery = listNotificationsQuery;
        this.unreadCountQuery = unreadCountQuery;
        this.markReadUseCase = markReadUseCase;
        this.markAllReadUseCase = markAllReadUseCase;
    }

    @GetMapping
    @Operation(summary = "Список уведомлений", description = "Фильтр all/unread")
    public ResponseEntity<PageResult<NotificationDto>> list(
            final Authentication authentication,
            @RequestParam(required = false, defaultValue = "all") final String filter,
            @RequestParam(defaultValue = "1") @Min(1) final int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) final int size
    ) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(
                listNotificationsQuery.execute(principal.userId(), filter, new PageQuery(page, size))
        );
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Число непрочитанных")
    public ResponseEntity<Long> unreadCount(final Authentication authentication) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(unreadCountQuery.execute(principal.userId()));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Пометить одно прочитанным")
    public ResponseEntity<Void> markRead(final Authentication authentication, @PathVariable final Integer id) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        markReadUseCase.execute(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    @Operation(summary = "Пометить все прочитанными")
    public ResponseEntity<Void> markAllRead(final Authentication authentication) {
        final JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        markAllReadUseCase.execute(principal.userId());
        return ResponseEntity.noContent().build();
    }
}

