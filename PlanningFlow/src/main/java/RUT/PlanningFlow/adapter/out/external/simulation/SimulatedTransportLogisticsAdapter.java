package RUT.PlanningFlow.adapter.out.external.simulation;

import RUT.PlanningFlow.adapter.out.common.OutboundCallRetry;
import RUT.PlanningFlow.application.port.out.TransportLogisticsPort;
import RUT.PlanningFlow.config.external.ExternalSupplyProperties;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.model.ExternalResource;
import RUT.PlanningFlow.domain.vo.DateTimeRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SimulatedTransportLogisticsAdapter implements TransportLogisticsPort {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatedTransportLogisticsAdapter.class);
    private static final String OPERATION = "external supply reserve";

    private final ExternalSupplySimulator simulator;
    private final ExternalSupplyProperties properties;

    public SimulatedTransportLogisticsAdapter(
            final ExternalSupplySimulator simulator,
            final ExternalSupplyProperties properties
    ) {
        this.simulator = simulator;
        this.properties = properties;
    }

    @Override
    public ExternalResource requestTransport(final String resourceName, final DateTimeRange window) {
        final ExternalReservationOutcome outcome = OutboundCallRetry.executeWithRetry(
                () -> simulator.reserve(ResourceType.TRANSPORT, resourceName, window),
                result -> result instanceof ExternalReservationOutcome.Failed failed && failed.isRetryable(),
                properties.maxAttempts(),
                OPERATION,
                LOG
        );
        return ExternalSupplyOutcomeMapper.toExternalResource(ResourceType.TRANSPORT, outcome);
    }
}
