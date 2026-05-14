package RUT.PlanningFlow.application.service.user;

import RUT.PlanningFlow.application.dto.user.UserProfileActivityStatsDto;
import RUT.PlanningFlow.application.dto.user.UserResponseDto;
import RUT.PlanningFlow.application.port.in.user.GetUserDetailsQuery;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.ResourceBookingRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.model.Role;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GetUserDetailsService implements GetUserDetailsQuery {

    private final UserRepositoryPort userRepository;
    private final AssignmentRepositoryPort assignmentRepository;
    private final EventRepositoryPort eventRepository;
    private final TaskRepositoryPort taskRepository;
    private final ResourceBookingRepositoryPort resourceBookingRepository;

    public GetUserDetailsService(
            final UserRepositoryPort userRepository,
            final AssignmentRepositoryPort assignmentRepository,
            final EventRepositoryPort eventRepository,
            final TaskRepositoryPort taskRepository,
            final ResourceBookingRepositoryPort resourceBookingRepository
    ) {
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(assignmentRepository, "Репозиторий назначений обязателен", "ASSIGNMENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(resourceBookingRepository, "Репозиторий бронирований обязателен", "BOOKING_REPOSITORY_REQUIRED");
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.eventRepository = eventRepository;
        this.taskRepository = taskRepository;
        this.resourceBookingRepository = resourceBookingRepository;
    }

    @Override
    public Optional<UserResponseDto> execute(final Integer userId) {
        DomainAssert.notNull(userId, "ID пользователя обязателен", "USER_ID_REQUIRED");
        final Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        final UserProfileActivityStatsDto stats = buildActivityStats(user.get());
        return Optional.of(UserResponseDto.from(user.get(), stats));
    }

    private UserProfileActivityStatsDto buildActivityStats(final User user) {
        final Integer uid = user.getId();
        if (uid == null) {
            return UserProfileActivityStatsDto.zero();
        }
        if (hasRole(user, UserRoles.COORDINATOR)) {
            return new UserProfileActivityStatsDto(
                    0,
                    0,
                    0.0,
                    toInt(eventRepository.countCompletedEventsWhereCreatorOrCoordinator(uid)),
                    toInt(taskRepository.countTasksAuthoredByUser(uid)),
                    toInt(resourceBookingRepository.countBookingsForEventsWhereCreatorOrCoordinator(uid)),
                    0,
                    0,
                    0
            );
        }
        if (hasRole(user, UserRoles.ORGANIZER)) {
            return new UserProfileActivityStatsDto(
                    0,
                    0,
                    0.0,
                    0,
                    0,
                    0,
                    toInt(eventRepository.countEventsCreatedByUser(uid)),
                    toInt(taskRepository.countTasksAuthoredByUser(uid)),
                    toInt(resourceBookingRepository.countBookingsForEventsWhereCreator(uid))
            );
        }
        if (hasRole(user, UserRoles.PARTICIPANT)) {
            return new UserProfileActivityStatsDto(
                    toInt(assignmentRepository.countCompletedTasksForUser(uid)),
                    toInt(assignmentRepository.countDistinctEventsParticipatedForUser(uid)),
                    taskRepository.sumCompletedWorkedHoursForUser(uid),
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
            );
        }
        return UserProfileActivityStatsDto.zero();
    }

    private static boolean hasRole(final User user, final UserRoles role) {
        for (final Role r : user.getRoles()) {
            if (r != null && r.getName() == role) {
                return true;
            }
        }
        return false;
    }

    private static int toInt(final long value) {
        if (value >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }
}
