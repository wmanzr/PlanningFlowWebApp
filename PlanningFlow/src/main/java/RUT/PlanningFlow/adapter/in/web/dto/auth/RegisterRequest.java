package RUT.PlanningFlow.adapter.in.web.dto.auth;

import RUT.PlanningFlow.domain.enums.UserRoles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank(message = "Имя пользователя обязательно") String username,
        @NotBlank(message = "Пароль обязателен")
        @Size(min = 8, max = 128, message = "Пароль должен быть от 8 до 128 символов")
        String password,
        @NotBlank(message = "Email обязателен")
        @Email(message = "Укажите корректный email")
        String email,
        @NotBlank(message = "ФИО обязательно") String fullName,
        @NotNull(message = "Дата рождения обязательна") LocalDate birthDate,
        @NotNull(message = "Роль обязательна") UserRoles role
) {
}
