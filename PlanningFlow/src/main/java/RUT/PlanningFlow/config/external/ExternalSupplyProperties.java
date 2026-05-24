package RUT.PlanningFlow.config.external;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "planningflow.external.supply")
@Validated
public record ExternalSupplyProperties(
        int connectTimeoutMs,
        int readTimeoutMs,
        int maxAttempts
) {
}
