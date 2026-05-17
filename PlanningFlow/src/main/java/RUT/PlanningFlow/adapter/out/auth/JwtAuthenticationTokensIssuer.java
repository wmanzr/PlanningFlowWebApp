package RUT.PlanningFlow.adapter.out.auth;

import RUT.PlanningFlow.application.port.in.dto.AuthTokenResponse;
import RUT.PlanningFlow.application.port.out.auth.AuthenticationTokensIssuer;
import RUT.PlanningFlow.application.port.out.repository.RefreshTokenRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.adapter.in.web.security.JwtProperties;
import RUT.PlanningFlow.adapter.in.web.security.JwtProvider;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Role;
import RUT.PlanningFlow.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationTokensIssuer implements AuthenticationTokensIssuer {

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepositoryPort refreshTokens;
    private final UserRepositoryPort users;

    public JwtAuthenticationTokensIssuer(
            final JwtProvider jwtProvider,
            final JwtProperties jwtProperties,
            final RefreshTokenRepositoryPort refreshTokens,
            final UserRepositoryPort users
    ) {
        this.jwtProvider = jwtProvider;
        this.jwtProperties = jwtProperties;
        this.refreshTokens = refreshTokens;
        this.users = users;
    }

    @Override
    public AuthTokenResponse issueAfterLogin(final User user) {
        return issueFreshPair(user);
    }

    @Override
    public AuthTokenResponse rotate(final String refreshTokenValue) {
        final String raw = refreshTokenValue == null ? "" : refreshTokenValue.trim();
        if (raw.isEmpty()) {
            throw new DomainException("Укажите refresh-токен", "REFRESH_TOKEN_REQUIRED");
        }
        try {
            final Claims claims = jwtProvider.parseAndValidateRefresh(raw);
            final int userId = JwtProvider.userIdFromSubject(claims);
            final String jti = JwtProvider.jtiFrom(claims);
            if (jti == null || jti.isBlank()) {
                throw new DomainException("Сессия истекла. Войдите снова.", "REFRESH_TOKEN_INVALID");
            }
            final Instant now = Instant.now();
            final int consumed = refreshTokens.consumeRefreshToken(jti, userId, now);
            if (consumed == 0) {
                throw new DomainException("Сессия истекла. Войдите снова.", "REFRESH_TOKEN_INVALID");
            }
            final User user = users.findById(userId)
                    .orElseThrow(() -> new DomainException("Сессия истекла. Войдите снова.", "REFRESH_TOKEN_INVALID"));
            return issueFreshPair(user);
        } catch (final JwtException e) {
            throw new DomainException("Сессия истекла. Войдите снова.", "REFRESH_TOKEN_INVALID");
        }
    }

    private AuthTokenResponse issueFreshPair(final User user) {
        final Integer id = user.getId();
        if (id == null) {
            throw new IllegalStateException("User id required for tokens");
        }
        final String jti = UUID.randomUUID().toString();
        final Instant refreshExpiry = Instant.now().plus(jwtProperties.refreshTokenTtl());
        refreshTokens.save(id, jti, refreshExpiry);
        final List<String> roles = roleAuthorities(user);
        final String access = jwtProvider.createAccessToken(id, user.getUsername(), roles);
        final String refresh = jwtProvider.createRefreshToken(id, jti, refreshExpiry);
        return new AuthTokenResponse(access, refresh);
    }

    private static List<String> roleAuthorities(final User user) {
        return user.getRoles().stream().map(Role::getName).map(en -> "ROLE_" + en.name()).toList();
    }
}