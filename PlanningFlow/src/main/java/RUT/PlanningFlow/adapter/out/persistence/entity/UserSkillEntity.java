package RUT.PlanningFlow.adapter.out.persistence.entity;

import RUT.PlanningFlow.domain.enums.SkillTier;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_skills")
public class UserSkillEntity extends BaseEntity implements Serializable {
    private UserEntity user;
    private SkillEntity skill;
    private SkillTier tier;
    private LocalDateTime verifiedAt;

    public UserSkillEntity() {
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public UserEntity getUser() {
        return user;
    }

    public void setUser(final UserEntity user) {
        this.user = user;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    public SkillEntity getSkill() {
        return skill;
    }

    public void setSkill(final SkillEntity skill) {
        this.skill = skill;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_tier", nullable = false, length = 32)
    public SkillTier getTier() {
        return tier;
    }

    public void setTier(final SkillTier tier) {
        this.tier = tier;
    }

    @Column(name = "verified_at")
    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(final LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
