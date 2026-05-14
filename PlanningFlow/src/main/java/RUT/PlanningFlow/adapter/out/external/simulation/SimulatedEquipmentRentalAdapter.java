package RUT.PlanningFlow.adapter.out.external.simulation;

import RUT.PlanningFlow.application.port.out.EquipmentRentalPort;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.model.ExternalResource;
import RUT.PlanningFlow.domain.vo.DateTimeRange;
import org.springframework.stereotype.Component;

@Component
public class SimulatedEquipmentRentalAdapter implements EquipmentRentalPort {

    private final ExternalSupplySimulator simulator;

    public SimulatedEquipmentRentalAdapter(final ExternalSupplySimulator simulator) {
        this.simulator = simulator;
    }

    @Override
    public ExternalResource request(final ResourceType type, final String resourceName, final DateTimeRange window) {
        final ExternalReservationOutcome outcome = simulator.reserve(type, resourceName, window);
        return ExternalSupplyOutcomeMapper.toExternalResource(type, outcome);
    }
}