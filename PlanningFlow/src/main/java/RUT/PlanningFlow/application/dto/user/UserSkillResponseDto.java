package RUT.PlanningFlow.application.dto.user;

import RUT.PlanningFlow.domain.enums.SkillTier;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.UserSkill;

import java.time.LocalDateTime;

public final class UserSkillResponseDto {
    private final Integer userSkillId;
    private final Integer skillId;
    private final String skillName;
    private final SkillTier tier;
    private final LocalDateTime verifiedAt;

    public UserSkillResponseDto(
            final Integer userSkillId,
            final Integer skillId,
            final String skillName,
            final SkillTier tier,
            final LocalDateTime verifiedAt
    ) {
        this.userSkillId = userSkillId;
        this.skillId = skillId;
        this.skillName = skillName;
        this.tier = tier;
        this.verifiedAt = verifiedAt;
    }

    public static UserSkillResponseDto from(final UserSkill us) {
        if (us == null) {
            return null;
        }
        final Skill skill = us.getSkill();
        return new UserSkillResponseDto(
                us.getId(),
                skill == null ? null : skill.getId(),
                skill == null ? null : skill.getName(),
                us.getTier(),
                us.getVerifiedAt()
        );
    }

    public Integer getUserSkillId() {
        return userSkillId;
    }

    public Integer getSkillId() {
        return skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public SkillTier getTier() {
        return tier;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }
}