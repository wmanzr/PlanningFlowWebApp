package RUT.PlanningFlow.config.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "planningflow.external.simulation")
public class ExternalSupplySimulationProperties {
    private boolean chaosEnabled = false;
    public boolean isChaosEnabled() {
        return chaosEnabled;
    }
    public void setChaosEnabled(final boolean chaosEnabled) {
        this.chaosEnabled = chaosEnabled;
    }
}