package RUT.PlanningFlow.application.service.task;

import RUT.PlanningFlow.application.dto.task.TaskResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.task.GetMyTasksQuery;
import RUT.PlanningFlow.application.dto.task.TaskAssignmentResponseDto;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetMyTasksService implements GetMyTasksQuery {

    private final TaskRepositoryPort taskRepository;
    private final UserRepositoryPort userRepository;
    private final AssignmentRepositoryPort assignmentRepository;

    public GetMyTasksService(
            final TaskRepositoryPort taskRepository,
            final UserRepositoryPort userRepository,
            final AssignmentRepositoryPort assignmentRepository
    ) {
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(assignmentRepository, "Репозиторий назначений обязателен", "ASSIGNMENT_REPOSITORY_REQUIRED");
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public PageResult<TaskResponseDto> execute(
            final Integer callerUserId,
            final Integer userId,
            final GetMyTasksQuery.AssignmentFilter filter,
            final LocalDateTime start,
            final LocalDateTime end,
            final String title,
            final PageQuery pageQuery
    ) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(userId, "ID пользователя обязателен", "USER_ID_REQUIRED");
        DomainAssert.notNull(filter, "Фильтр обязателен", "ASSIGNMENT_FILTER_REQUIRED");
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");

        final User caller = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!PlanningAccessPolicy.hasRole(caller, UserRoles.ADMIN) && !callerUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final List<AssignStatus> statuses = switch (filter) {
            case ALL -> List.of(AssignStatus.PENDING, AssignStatus.ACCEPTED, AssignStatus.REJECTED);
            case CONFIRMED -> List.of(AssignStatus.ACCEPTED);
            case NOT_CONFIRMED -> List.of(AssignStatus.PENDING);
        };

        final PageResult<Task> page = (start == null || end == null)
                ? taskRepository.findTasksForUser(userId, statuses, title, pageQuery)
                : taskRepository.findTasksForUserBetween(userId, statuses, start, end, title, pageQuery);

        final List<TaskResponseDto> items = new ArrayList<>(page.items().size());
        for (final Task t : page.items()) {
            final TaskAssignmentResponseDto viewerAssignment =
                    assignmentRepository.findLatestForTaskAndUser(t.getId(), userId)
                            .map(TaskAssignmentResponseDto::from)
                            .orElse(null);
            items.add(TaskResponseDto.from(t, List.of(), null, viewerAssignment));
        }
        return new PageResult<>(items, page.totalElements(), page.totalPages());
    }
}
