package RUT.PlanningFlow.adapter.out.external.simulation;

import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.ExternalResource;

final class ExternalSupplyOutcomeMapper {

    private ExternalSupplyOutcomeMapper() {
    }

    static ExternalResource toExternalResource(final ResourceType type, final ExternalReservationOutcome outcome) {
        return switch (outcome) {
            case ExternalReservationOutcome.Confirmed c -> new ExternalResource(
                    null,
                    c.resourceLabel(),
                    type,
                    c.supplierReference()
            );
            case ExternalReservationOutcome.Failed f -> switch (f.category()) {
                case NO_CAPACITY -> null;
                case UPSTREAM_UNAVAILABLE -> throw new DomainException(
                        "Внешний поставщик временно недоступен",
                        "EXTERNAL_SUPPLIER_UNAVAILABLE"
                );
                case UPSTREAM_TIMEOUT -> throw new DomainException(
                        "Таймаут при обращении к внешнему поставщику",
                        "EXTERNAL_SUPPLIER_TIMEOUT"
                );
            };
        };
    }
}