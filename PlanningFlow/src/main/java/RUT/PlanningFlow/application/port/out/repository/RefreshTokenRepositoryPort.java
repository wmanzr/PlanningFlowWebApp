package RUT.PlanningFlow.application.port.out.repository;

import java.time.Instant;

public interface RefreshTokenRepositoryPort {
    void save(int userId, String jti, Instant expiresAt);
    int consumeRefreshToken(String jti, int userId, Instant now);
}