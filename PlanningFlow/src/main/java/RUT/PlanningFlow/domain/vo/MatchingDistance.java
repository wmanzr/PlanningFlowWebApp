package RUT.PlanningFlow.domain.vo;

import RUT.PlanningFlow.domain.utils.DomainAssert;

public record MatchingDistance(double referenceRadiusMeters) {

    public static final MatchingDistance BUILDING_SCALE = new MatchingDistance(500.0d);
    public static final MatchingDistance CITY_SCALE = new MatchingDistance(35_000.0d);
    public static final MatchingDistance REGION_SCALE = new MatchingDistance(65_000.0d);

    public MatchingDistance {
        DomainAssert.isTrue(
                referenceRadiusMeters > 0.0d,
                "Опорный радиус для гео-нормализации должен быть положительным",
                "INVALID_MATCHING_DISTANCE_RADIUS"
        );
    }
}
