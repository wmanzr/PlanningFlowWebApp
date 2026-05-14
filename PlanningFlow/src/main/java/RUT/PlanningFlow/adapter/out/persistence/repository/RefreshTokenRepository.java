package RUT.PlanningFlow.adapter.out.persistence.repository;

import RUT.PlanningFlow.adapter.out.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface RefreshTokenRepository extends BaseRepository<RefreshTokenEntity, Integer> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            DELETE FROM RefreshTokenEntity r
            WHERE r.jti = :jti AND r.user.id = :userId AND r.expiresAt > :now
            """
    )
    int deleteActiveToken(
            @Param("jti") String jti,
            @Param("userId") int userId,
            @Param("now") Instant now
    );
}