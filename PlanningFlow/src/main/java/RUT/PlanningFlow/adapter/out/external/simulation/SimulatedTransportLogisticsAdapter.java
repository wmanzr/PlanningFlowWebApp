package RUT.PlanningFlow.adapter.out.external.simulation;

import RUT.PlanningFlow.application.port.out.TransportLogisticsPort;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.model.ExternalResource;
import RUT.PlanningFlow.domain.vo.DateTimeRange;
import org.springframework.stereotype.Component;

@Component
public class SimulatedTransportLogisticsAdapter implements TransportLogisticsPort {
    private final ExternalSupplySimulator simulator;

    public SimulatedTransportLogisticsAdapter(final ExternalSupplySimulator simulator) {
        this.simulator = simulator;
    }

    @Override
    public ExternalResource requestTransport(final String resourceName, final DateTimeRange window) {
        final ExternalReservationOutcome outcome = simulator.reserve(ResourceType.TRANSPORT, resourceName, window);
        return ExternalSupplyOutcomeMapper.toExternalResource(ResourceType.TRANSPORT, outcome);
    }
}