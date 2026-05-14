package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.RefreshTokenEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.UserEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.RefreshTokenRepository;
import RUT.PlanningFlow.adapter.out.persistence.repository.UserRepository;
import RUT.PlanningFlow.application.port.out.repository.RefreshTokenRepositoryPort;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class RefreshTokenAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenRepository refreshTokens;
    private final UserRepository users;

    public RefreshTokenAdapter(final RefreshTokenRepository refreshTokens, final UserRepository users) {
        this.refreshTokens = refreshTokens;
        this.users = users;
    }

    @Override
    public void save(final int userId, final String jti, final Instant expiresAt) {
        final UserEntity user = users.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
        final RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUser(user);
        entity.setJti(jti);
        entity.setExpiresAt(expiresAt);
        refreshTokens.save(entity);
    }

    @Override
    public int consumeRefreshToken(final String jti, final int userId, final Instant now) {
        return refreshTokens.deleteActiveToken(jti, userId, now);
    }
}