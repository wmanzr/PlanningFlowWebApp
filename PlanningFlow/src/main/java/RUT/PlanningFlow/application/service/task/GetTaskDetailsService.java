package RUT.PlanningFlow.application.service.task;

import RUT.PlanningFlow.application.dto.task.TaskAssignmentResponseDto;
import RUT.PlanningFlow.application.dto.task.TaskResponseDto;
import RUT.PlanningFlow.application.port.in.task.GetTaskDetailsQuery;
import RUT.PlanningFlow.application.port.out.repository.AssignmentRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.model.Assignment;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import RUT.PlanningFlow.domain.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GetTaskDetailsService implements GetTaskDetailsQuery {

    private final TaskRepositoryPort taskRepository;
    private final UserRepositoryPort userRepository;
    private final AssignmentRepositoryPort assignmentRepository;
    private final TaskResponseDtoMapper taskResponseDtoMapper;
    private final TaskAssignmentResponseDtoMapper taskAssignmentResponseDtoMapper;

    public GetTaskDetailsService(
            final TaskRepositoryPort taskRepository,
            final UserRepositoryPort userRepository,
            final AssignmentRepositoryPort assignmentRepository,
            final TaskResponseDtoMapper taskResponseDtoMapper,
            final TaskAssignmentResponseDtoMapper taskAssignmentResponseDtoMapper
    ) {
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(assignmentRepository, "Репозиторий назначений обязателен", "ASSIGNMENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(taskResponseDtoMapper, "Маппер ответа по задаче обязателен", "TASK_RESPONSE_DTO_MAPPER_REQUIRED");
        DomainAssert.notNull(taskAssignmentResponseDtoMapper, "Маппер ответа по назначению обязателен", "TASK_ASSIGNMENT_RESPONSE_DTO_MAPPER_REQUIRED");
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.taskResponseDtoMapper = taskResponseDtoMapper;
        this.taskAssignmentResponseDtoMapper = taskAssignmentResponseDtoMapper;
    }

    @Override
    public Optional<TaskResponseDto> execute(final Integer callerUserId, final Integer taskId) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(taskId, "ID задачи обязателен", "TASK_ID_REQUIRED");

        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new DomainException("Пользователь не найден", "USER_NOT_FOUND"));
        final Optional<Task> exTask = taskRepository.findById(taskId);
        if (exTask.isEmpty()) {
            return Optional.empty();
        }
        final Task task = exTask.get();
        final boolean assignedToTask = assignmentRepository.existsAssignmentForUserOnTask(actor.getId(), taskId);
        PlanningAccessPolicy.assertCanViewTask(actor, task, assignedToTask);

        final List<Assignment> assignmentRows = assignmentRepository.findByTaskId(taskId);
        final List<TaskAssignmentResponseDto> assignmentDtos = new ArrayList<>();
        for (final Assignment a : assignmentRows) {
            final TaskAssignmentResponseDto dto = taskAssignmentResponseDtoMapper.toResponse(a);
            if (dto != null) {
                assignmentDtos.add(dto);
            }
        }
        final int requiredSlots = taskResponseDtoMapper.countActiveAssignmentSlots(assignmentDtos);
        return Optional.of(taskResponseDtoMapper.toResponse(task, assignmentDtos, requiredSlots));
    }
}
