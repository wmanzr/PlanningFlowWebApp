package RUT.PlanningFlow.application.port.in.auth;

import RUT.PlanningFlow.application.port.in.dto.AuthTokenResponse;

public interface LoginUseCase {
    AuthTokenResponse login(String username, String rawPassword);
}