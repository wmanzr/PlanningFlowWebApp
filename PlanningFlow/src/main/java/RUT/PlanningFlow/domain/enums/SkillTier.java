package RUT.PlanningFlow.domain.enums;

import RUT.PlanningFlow.domain.utils.DomainAssert;

public enum SkillTier {
    NOVICE(0.3d),
    PRACTITIONER(0.65d),
    EXPERT(1.0d);

    private final double weight;

    SkillTier(final double weight) {
        DomainAssert.isTrue(
                weight >= 0.0d && weight <= 1.0d,
                "Вес уровня навыка должен быть в диапазоне [0;1]",
                "INVALID_SKILL_TIER_WEIGHT"
        );
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}
