package RUT.PlanningFlow.domain.enums;

import RUT.PlanningFlow.domain.utils.DomainAssert;

public enum MatchingMode {
    STANDARD(0.5d),
    CRITICAL(0.9d);

    private final double relatedSkillFactor;

    MatchingMode(final double relatedSkillFactor) {
        DomainAssert.isTrue(
                relatedSkillFactor >= 0.0d && relatedSkillFactor <= 1.0d,
                "Коэффициент смежного навыка должен быть в диапазоне [0;1]",
                "INVALID_RELATED_SKILL_FACTOR"
        );
        this.relatedSkillFactor = relatedSkillFactor;
    }

    public double getRelatedSkillFactor() {
        return relatedSkillFactor;
    }
}
