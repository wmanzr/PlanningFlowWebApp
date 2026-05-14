package RUT.PlanningFlow.adapter.out.external.simulation;

public sealed interface ExternalReservationOutcome permits ExternalReservationOutcome.Confirmed, ExternalReservationOutcome.Failed {

    record Confirmed(String supplierReference, String resourceLabel) implements ExternalReservationOutcome {}

    record Failed(FailureCategory category, String detail) implements ExternalReservationOutcome {}

    enum FailureCategory {
        NO_CAPACITY,
        UPSTREAM_UNAVAILABLE,
        UPSTREAM_TIMEOUT
    }
}