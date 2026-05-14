package RUT.PlanningFlow.application.port.out.auth;

import RUT.PlanningFlow.application.port.in.dto.AuthTokenResponse;
import RUT.PlanningFlow.domain.model.User;

public interface AuthenticationTokensIssuer {
    AuthTokenResponse issueAfterLogin(User user);
    AuthTokenResponse rotate(String refreshTokenValue);
}