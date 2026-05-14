package RUT.PlanningFlow.application.dto.user;

import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.model.Role;
import RUT.PlanningFlow.domain.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class UserResponseDto {
    private final Integer id;
    private final String username;
    private final String email;
    private final String fullName;
    private final LocalDate birthDate;
    private final List<UserRoles> roles;
    private final int completedTasksCount;
    private final int eventsParticipatedCount;
    private final double totalWorkedHours;
    private final int coordinatorCompletedEventsCount;
    private final int coordinatorTasksCreatedCount;
    private final int coordinatorBookingsCreatedCount;
    private final int organizerEventsCreatedCount;
    private final int organizerTasksCreatedCount;
    private final int organizerBookingsCreatedCount;

    public UserResponseDto(
            final Integer id,
            final String username,
            final String email,
            final String fullName,
            final LocalDate birthDate,
            final List<UserRoles> roles,
            final int completedTasksCount,
            final int eventsParticipatedCount,
            final double totalWorkedHours,
            final int coordinatorCompletedEventsCount,
            final int coordinatorTasksCreatedCount,
            final int coordinatorBookingsCreatedCount,
            final int organizerEventsCreatedCount,
            final int organizerTasksCreatedCount,
            final int organizerBookingsCreatedCount
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.roles = roles == null ? List.of() : List.copyOf(roles);
        this.completedTasksCount = completedTasksCount;
        this.eventsParticipatedCount = eventsParticipatedCount;
        this.totalWorkedHours = totalWorkedHours;
        this.coordinatorCompletedEventsCount = coordinatorCompletedEventsCount;
        this.coordinatorTasksCreatedCount = coordinatorTasksCreatedCount;
        this.coordinatorBookingsCreatedCount = coordinatorBookingsCreatedCount;
        this.organizerEventsCreatedCount = organizerEventsCreatedCount;
        this.organizerTasksCreatedCount = organizerTasksCreatedCount;
        this.organizerBookingsCreatedCount = organizerBookingsCreatedCount;
    }

    public static UserResponseDto from(final User user) {
        return from(user, UserProfileActivityStatsDto.zero());
    }

    public static UserResponseDto from(final User user, final UserProfileActivityStatsDto stats) {
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

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public List<UserRoles> getRoles() {
        return roles;
    }

    public int getCompletedTasksCount() {
        return completedTasksCount;
    }

    public int getEventsParticipatedCount() {
        return eventsParticipatedCount;
    }

    public double getTotalWorkedHours() {
        return totalWorkedHours;
    }

    public int getCoordinatorCompletedEventsCount() {
        return coordinatorCompletedEventsCount;
    }

    public int getCoordinatorTasksCreatedCount() {
        return coordinatorTasksCreatedCount;
    }

    public int getCoordinatorBookingsCreatedCount() {
        return coordinatorBookingsCreatedCount;
    }

    public int getOrganizerEventsCreatedCount() {
        return organizerEventsCreatedCount;
    }

    public int getOrganizerTasksCreatedCount() {
        return organizerTasksCreatedCount;
    }

    public int getOrganizerBookingsCreatedCount() {
        return organizerBookingsCreatedCount;
    }
}
