package RUT.PlanningFlow.adapter.out.external.simulation;

import RUT.PlanningFlow.adapter.out.common.OutboundCallRetry;
import RUT.PlanningFlow.application.port.out.EquipmentRentalPort;
import RUT.PlanningFlow.config.external.ExternalSupplyProperties;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.model.ExternalResource;
import RUT.PlanningFlow.domain.vo.DateTimeRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SimulatedEquipmentRentalAdapter implements EquipmentRentalPort {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatedEquipmentRentalAdapter.class);
    private static final String OPERATION = "external supply reserve";

    private final ExternalSupplySimulator simulator;
    private final ExternalSupplyProperties properties;

    public SimulatedEquipmentRentalAdapter(
            final ExternalSupplySimulator simulator,
            final ExternalSupplyProperties properties
    ) {
        this.simulator = simulator;
        this.properties = properties;
    }

    @Override
    public ExternalResource request(final ResourceType type, final String resourceName, final DateTimeRange window) {
        final ExternalReservationOutcome outcome = OutboundCallRetry.executeWithRetry(
                () -> simulator.reserve(type, resourceName, window),
                result -> result instanceof ExternalReservationOutcome.Failed failed && failed.isRetryable(),
                properties.maxAttempts(),
                OPERATION,
                LOG
        );
        return ExternalSupplyOutcomeMapper.toExternalResource(type, outcome);
    }
}
