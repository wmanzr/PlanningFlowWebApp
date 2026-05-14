package RUT.PlanningFlow.application.service.auth;

import RUT.PlanningFlow.application.port.in.auth.LoginUseCase;
import RUT.PlanningFlow.application.port.in.auth.RefreshTokenUseCase;
import RUT.PlanningFlow.application.port.in.dto.AuthTokenResponse;
import RUT.PlanningFlow.application.port.out.auth.AuthenticationTokensIssuer;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthApplicationService implements LoginUseCase, RefreshTokenUseCase {

    private final UserRepositoryPort users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationTokensIssuer tokens;

    public AuthApplicationService(
            final UserRepositoryPort users,
            final PasswordEncoder passwordEncoder,
            final AuthenticationTokensIssuer tokens
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Override
    public AuthTokenResponse login(final String username, final String rawPassword) {
        final String userKey = username == null ? "" : username.trim();
        final var user = users.findByUsername(userKey)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return tokens.issueAfterLogin(user);
    }

    @Override
    @Transactional
    public AuthTokenResponse refresh(final String refreshTokenValue) {
        return tokens.rotate(refreshTokenValue);
    }
}