package RUT.PlanningFlow.adapter.in.web.controller;

import RUT.PlanningFlow.adapter.in.web.dto.auth.LoginRequest;
import RUT.PlanningFlow.adapter.in.web.dto.auth.RefreshTokenRequest;
import RUT.PlanningFlow.adapter.in.web.dto.auth.RegisterRequest;
import RUT.PlanningFlow.application.port.in.auth.LoginUseCase;
import RUT.PlanningFlow.application.port.in.auth.RefreshTokenUseCase;
import RUT.PlanningFlow.application.port.in.auth.RegisterUserUseCase;
import RUT.PlanningFlow.application.port.in.dto.AuthTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Аутентификация", description = "Выдача JWT без предварительной авторизации")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final RegisterUserUseCase registerUserUseCase;

    public AuthController(
            final LoginUseCase loginUseCase,
            final RefreshTokenUseCase refreshTokenUseCase,
            final RegisterUserUseCase registerUserUseCase
    ) {
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping("/login")
    @Operation(summary = "Вход по логину и паролю", description = "Возвращает access и refresh токены", security = {})
    public AuthTokenResponse login(@Valid @RequestBody final LoginRequest body) {
        return loginUseCase.login(body.username(), body.password());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновление access по refresh", description = "Новая пара токенов", security = {})
    public AuthTokenResponse refresh(@Valid @RequestBody final RefreshTokenRequest body) {
        return refreshTokenUseCase.refresh(body.refreshToken());
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Создание учетной записи и выдача токенов", security = {})
    public AuthTokenResponse register(@Valid @RequestBody final RegisterRequest body) {
        return registerUserUseCase.register(
                body.username(),
                body.password(),
                body.email(),
                body.fullName(),
                body.birthDate(),
                body.role()
        );
    }
}
