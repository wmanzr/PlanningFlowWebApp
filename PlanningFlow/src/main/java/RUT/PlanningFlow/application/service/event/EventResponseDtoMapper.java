package RUT.PlanningFlow.application.service.event;

import RUT.PlanningFlow.application.dto.event.EventResponseDto;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public final class EventResponseDtoMapper {

    public EventResponseDto toResponse(final Event event) {
        return toResponse(event, 0L);
    }

    public EventResponseDto toResponse(final Event event, final long tasksCount) {
        if (event == null) {
            return null;
        }

        final List<Integer> coordinatorIds = new ArrayList<>();
        final List<User> coordinators = event.getCoordinators();
        if (coordinators != null) {
            for (final User u : coordinators) {
                if (u != null && u.getId() != null) {
                    coordinatorIds.add(u.getId());
                }
            }
        }

        final User creator = event.getCreator();
        return new EventResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStatus(),
                event.getStartDate(),
                event.getEndDate(),
                event.getLatitude(),
                event.getLongitude(),
                creator == null ? null : creator.getId(),
                coordinatorIds,
                tasksCount
        );
    }
}
