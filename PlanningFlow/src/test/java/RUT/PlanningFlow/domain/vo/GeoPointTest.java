package RUT.PlanningFlow.domain.vo;

import RUT.PlanningFlow.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeoPointTest {

    @Test
    void haversine_same_point_is_zero() {
        final GeoPoint p = new GeoPoint(55.75, 37.61);

        assertThat(p.haversineDistanceMetersTo(p)).isZero();
    }

    @Test
    void haversine_between_two_points_positive() {
        final GeoPoint a = new GeoPoint(55.75, 37.61);
        final GeoPoint b = new GeoPoint(55.76, 37.62);

        assertThat(a.haversineDistanceMetersTo(b)).isPositive();
    }

    @Test
    void haversine_requires_destination() {
        final GeoPoint p = new GeoPoint(0.0, 0.0);

        assertThatThrownBy(() -> p.haversineDistanceMetersTo(null))
                .hasFieldOrPropertyWithValue("errorCode", "GEO_POINT_REQUIRED");
    }

    @Test
    void invalid_latitude_rejected() {
        assertThatThrownBy(() -> new GeoPoint(91.0, 0.0))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_LATITUDE");
    }

    @Test
    void invalid_longitude_rejected() {
        assertThatThrownBy(() -> new GeoPoint(0.0, 190.0))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_LONGITUDE");
    }

    @Test
    void equals_accepts_only_geo_point_instances() {
        final GeoPoint p = new GeoPoint(10.0, 20.0);

        assertThat(p.equals("not-a-point")).isFalse();
    }
}
