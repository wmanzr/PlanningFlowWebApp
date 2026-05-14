package RUT.PlanningFlow.application.port.in.auth;

import RUT.PlanningFlow.application.port.in.dto.AuthTokenResponse;
import RUT.PlanningFlow.domain.enums.UserRoles;

import java.time.LocalDate;

public interface RegisterUserUseCase {
    AuthTokenResponse register(
            String username,
            String rawPassword,
            String email,
            String fullName,
            LocalDate birthDate,
            UserRoles role
    );
}
