package RUT.PlanningFlow.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity extends BaseEntity {

    private UserEntity user;
    private String jti;
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public UserEntity getUser() {
        return user;
    }

    public void setUser(final UserEntity user) {
        this.user = user;
    }

    @Column(nullable = false, unique = true, length = 64)
    public String getJti() {
        return jti;
    }

    public void setJti(final String jti) {
        this.jti = jti;
    }

    @Column(nullable = false)
    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(final Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}