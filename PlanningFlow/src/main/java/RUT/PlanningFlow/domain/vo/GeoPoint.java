package RUT.PlanningFlow.domain.vo;

import RUT.PlanningFlow.domain.utils.DomainAssert;

import java.util.Objects;

public final class GeoPoint {
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private final double latitude;
    private final double longitude;

    public GeoPoint(final double latitude, final double longitude) {
        DomainAssert.isTrue(latitude >= -90.0 && latitude <= 90.0,
        "Широта должна быть в диапазоне [-90; 90]", "INVALID_LATITUDE");
        DomainAssert.isTrue(longitude >= -180.0 && longitude <= 180.0,
        "Долгота должна быть в диапазоне [-180; 180]", "INVALID_LONGITUDE");
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double haversineDistanceMetersTo(final GeoPoint other) {
        DomainAssert.notNull(other, "Точка назначения обязательна", "GEO_POINT_REQUIRED");

        final double lat1 = Math.toRadians(this.latitude);
        final double lat2 = Math.toRadians(other.latitude);
        final double deltaLat = Math.toRadians(other.latitude - this.latitude);
        final double deltaLon = Math.toRadians(other.longitude - this.longitude);

        final double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GeoPoint other)) {
            return false;
        }
        return Double.compare(other.latitude, latitude) == 0
                && Double.compare(other.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}
