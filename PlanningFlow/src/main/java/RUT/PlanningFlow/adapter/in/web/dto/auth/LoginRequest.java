package RUT.PlanningFlow.adapter.in.web.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Логин обязателен") String username,
        @NotBlank(message = "Пароль обязателен") String password
) {
}