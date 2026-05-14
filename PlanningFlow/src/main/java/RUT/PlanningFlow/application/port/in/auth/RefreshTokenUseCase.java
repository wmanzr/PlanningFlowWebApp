package RUT.PlanningFlow.application.port.in.auth;

import RUT.PlanningFlow.application.port.in.dto.AuthTokenResponse;

public interface RefreshTokenUseCase {
    AuthTokenResponse refresh(String refreshTokenValue);
}