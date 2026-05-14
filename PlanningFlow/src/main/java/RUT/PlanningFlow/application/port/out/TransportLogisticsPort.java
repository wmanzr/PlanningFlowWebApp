package RUT.PlanningFlow.application.port.out;

import RUT.PlanningFlow.domain.model.ExternalResource;
import RUT.PlanningFlow.domain.vo.DateTimeRange;

public interface TransportLogisticsPort {
    ExternalResource requestTransport(String resourceName, DateTimeRange window);
}

