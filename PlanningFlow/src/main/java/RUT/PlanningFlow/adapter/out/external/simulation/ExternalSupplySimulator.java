package RUT.PlanningFlow.adapter.out.external.simulation;

import RUT.PlanningFlow.adapter.out.external.simulation.ExternalReservationOutcome.Failed;
import RUT.PlanningFlow.config.external.ExternalSupplySimulationProperties;
import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.vo.DateTimeRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ExternalSupplySimulator {

    private static final Logger log = LoggerFactory.getLogger(ExternalSupplySimulator.class);

    private static final double TRANSIENT_FAILURE_RATE = 0.06;
    private static final double NO_CAPACITY_FAILURE_RATE = 0.07;
    private static final double SLOW_RESPONSE_PROBABILITY = 0.12;
    
    private static final int SLOW_RESPONSE_DELAY_MS = 3_000;

    public static final String HOOK_NO_CAPACITY = "__SIM_NO_CAPACITY__";
    public static final String HOOK_UNAVAILABLE = "__SIM_503__";
    public static final String HOOK_TIMEOUT = "__SIM_TIMEOUT__";

    private final ExternalSupplySimulationProperties properties;

    public ExternalSupplySimulator(final ExternalSupplySimulationProperties properties) {
        this.properties = properties;
    }

    public ExternalReservationOutcome reserve(
            final ResourceType resourceType,
            final String resourceName,
            final DateTimeRange window
    ) {
        Objects.requireNonNull(resourceType, "resourceType");
        Objects.requireNonNull(window, "window");
        final String name = resourceName == null ? "" : resourceName.trim();

        final ExternalReservationOutcome hook = deterministicHook(name);
        if (hook != null) {
            log.debug("external supply simulator: deterministic outcome for name={}", name);
            return hook;
        }

        if (properties.isChaosEnabled()) {
            maybeSlowDown();
            final ThreadLocalRandom rnd = ThreadLocalRandom.current();
            final double r = rnd.nextDouble();
            if (r < TRANSIENT_FAILURE_RATE) {
                log.debug("external supply simulator: simulated upstream unavailable");
                return new Failed(
                        ExternalReservationOutcome.FailureCategory.UPSTREAM_UNAVAILABLE,
                        "simulated upstream HTTP 503"
                );
            }
            if (r < TRANSIENT_FAILURE_RATE + NO_CAPACITY_FAILURE_RATE) {
                log.debug("external supply simulator: simulated no capacity");
                return new Failed(
                        ExternalReservationOutcome.FailureCategory.NO_CAPACITY,
                        "simulated supplier has no free units"
                );
            }
        }

        final String label = name.isEmpty() ? defaultLabel(resourceType) : name;
        final String ref = "sim-" + resourceType.name().toLowerCase() + "-" + UUID.randomUUID();
        log.debug("external supply simulator: confirmed booking ref={}", ref);
        return new ExternalReservationOutcome.Confirmed(ref, label);
    }

    private static ExternalReservationOutcome deterministicHook(final String name) {
        if (HOOK_NO_CAPACITY.equals(name)) {
            return new Failed(ExternalReservationOutcome.FailureCategory.NO_CAPACITY, "hook: explicit no capacity");
        }
        if (HOOK_UNAVAILABLE.equals(name)) {
            return new Failed(ExternalReservationOutcome.FailureCategory.UPSTREAM_UNAVAILABLE, "hook: explicit 503");
        }
        if (HOOK_TIMEOUT.equals(name)) {
            return new Failed(ExternalReservationOutcome.FailureCategory.UPSTREAM_TIMEOUT, "hook: explicit timeout");
        }
        return null;
    }

    private void maybeSlowDown() {
        if (ThreadLocalRandom.current().nextDouble() >= SLOW_RESPONSE_PROBABILITY) {
            return;
        }
        try {
            Thread.sleep(SLOW_RESPONSE_DELAY_MS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("external supply simulator: interrupted during simulated delay");
        }
    }

    private static String defaultLabel(final ResourceType type) {
        return switch (type) {
            case TRANSPORT -> "external-transport";
            case EQUIPMENT -> "external-equipment";
            case MATERIAL -> "external-material";
        };
    }
}