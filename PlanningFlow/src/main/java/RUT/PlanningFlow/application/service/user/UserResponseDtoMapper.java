package RUT.PlanningFlow.application.service.user;

import RUT.PlanningFlow.application.dto.user.UserProfileActivityStatsDto;
import RUT.PlanningFlow.application.dto.user.UserResponseDto;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.model.Role;
import RUT.PlanningFlow.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public final class UserResponseDtoMapper {

    public UserResponseDto toResponse(final User user) {
        return toResponse(user, UserProfileActivityStatsDto.zero());
    }

    public UserResponseDto toResponse(final User user, final UserProfileActivityStatsDto stats) {
        if (user == null) {
            return null;
        }
        final UserProfileActivityStatsDto s = stats == null ? UserProfileActivityStatsDto.zero() : stats;
        final List<UserRoles> roleNames = new ArrayList<>();
        for (final Role r : user.getRoles()) {
            if (r != null && r.getName() != null) {
                roleNames.add(r.getName());
            }
        }
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getBirthDate(),
                roleNames,
                s.completedTasksCount(),
                s.eventsParticipatedCount(),
                s.totalWorkedHours(),
                s.coordinatorCompletedEventsCount(),
                s.coordinatorTasksCreatedCount(),
                s.coordinatorBookingsCreatedCount(),
                s.organizerEventsCreatedCount(),
                s.organizerTasksCreatedCount(),
                s.organizerBookingsCreatedCount()
        );
    }
}
