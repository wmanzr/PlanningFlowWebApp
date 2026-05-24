package RUT.PlanningFlow.adapter.out.external.simulation;

public sealed interface ExternalReservationOutcome permits ExternalReservationOutcome.Confirmed, ExternalReservationOutcome.Failed {

    record Confirmed(String supplierReference, String resourceLabel) implements ExternalReservationOutcome {}

    record Failed(FailureCategory category, String detail) implements ExternalReservationOutcome {
        public boolean isRetryable() {
            return category == FailureCategory.UPSTREAM_UNAVAILABLE
                    || category == FailureCategory.UPSTREAM_TIMEOUT;
        }
    }

    enum FailureCategory {
        NO_CAPACITY,
        UPSTREAM_UNAVAILABLE,
        UPSTREAM_TIMEOUT
    }
}