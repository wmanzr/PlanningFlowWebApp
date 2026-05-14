package RUT.PlanningFlow.application.port.in.event;

import RUT.PlanningFlow.application.dto.event.EventDashboardResponseDto;

public interface GetEventDashboardQuery {
    EventDashboardResponseDto execute(Integer callerUserId, Integer eventId);
}
