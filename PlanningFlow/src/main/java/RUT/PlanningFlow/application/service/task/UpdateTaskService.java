package RUT.PlanningFlow.application.service.task;

import RUT.PlanningFlow.application.port.in.task.UpdateTaskUseCase;
import RUT.PlanningFlow.application.port.out.repository.SkillRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UpdateTaskService implements UpdateTaskUseCase {

    private final TaskRepositoryPort taskRepository;
    private final SkillRepositoryPort skillRepository;
    private final UserRepositoryPort userRepository;

    public UpdateTaskService(
            final TaskRepositoryPort taskRepository,
            final SkillRepositoryPort skillRepository,
            final UserRepositoryPort userRepository
    ) {
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(skillRepository, "Репозиторий навыков обязателен", "SKILL_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        this.taskRepository = taskRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Integer> execute(
            final Integer callerUserId,
            final Integer taskId,
            final String newTitle,
            final LocalDateTime newStartTime,
            final LocalDateTime newEndTime,
            final GeoPoint newLocation,
            final boolean clearLocation,
            final List<Integer> requiredSkillIds,
            final List<Integer> dependencyIds
    ) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(taskId, "ID задачи обязателен", "TASK_ID_REQUIRED");
        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Optional<Task> exTask = taskRepository.findById(taskId);
        if (exTask.isEmpty()) {
            return Optional.empty();
        }

        final Task task = exTask.get();
        PlanningAccessPolicy.assertCanManageTaskAsPlanner(actor, task);
        task.assertAllowsPlannerMutations();

        if (newTitle != null) {
            final String trimmed = newTitle.trim();
            if (taskRepository.existsByEventIdAndTitleIgnoreCaseAndIdNot(
                    task.getEvent().getId(),
                    trimmed,
                    task.getId()
            )) {
                throw new DomainException(
                        "На этом мероприятии уже есть задача с таким названием",
                        "TASK_DUPLICATE_TITLE_IN_EVENT"
                );
            }
            task.rename(trimmed);
        }
        if (newStartTime != null || newEndTime != null) {
            DomainAssert.notNull(newStartTime, "Дата начала обязательна при изменении дат", "TASK_START_DATE_REQUIRED");
            DomainAssert.notNull(newEndTime, "Дата окончания обязательна при изменении дат", "TASK_END_DATE_REQUIRED");
            task.moveSchedule(newStartTime, newEndTime);
        }
        if (clearLocation) {
            task.clearLocation();
        } else if (newLocation != null) {
            task.updateLocation(newLocation);
        }

        if (requiredSkillIds != null) {
            final Set<Integer> desiredIds = new HashSet<>();
            for (final Integer id : requiredSkillIds) {
                DomainAssert.notNull(id, "ID навыка обязателен", "SKILL_ID_REQUIRED");
                desiredIds.add(id);
            }

            for (final Skill existingSkill : List.copyOf(task.getRequiredSkills())) {
                if (existingSkill.getId() == null) {
                    continue;
                }
                if (!desiredIds.contains(existingSkill.getId())) {
                    task.removeRequiredSkill(existingSkill);
                }
            }

            for (final Integer desiredId : desiredIds) {
                final boolean alreadyHas = task.getRequiredSkills().stream()
                        .anyMatch(s -> s != null && desiredId.equals(s.getId()));
                if (alreadyHas) {
                    continue;
                }
                final Skill skill = skillRepository.findById(desiredId)
                        .orElseThrow(() -> new DomainException("Навык не найден", "SKILL_NOT_FOUND"));
                task.addRequiredSkill(skill);
            }
        }

        if (dependencyIds != null) {
            final Set<Integer> desiredIds = new HashSet<>();
            for (final Integer id : dependencyIds) {
                DomainAssert.notNull(id, "ID зависимости обязателен", "DEPENDENCY_ID_REQUIRED");
                desiredIds.add(id);
            }

            for (final Task existingDep : List.copyOf(task.getDependencies())) {
                if (existingDep == null || existingDep.getId() == null) {
                    continue;
                }
                if (!desiredIds.contains(existingDep.getId())) {
                    task.removeDependency(existingDep);
                }
            }

            for (final Integer desiredId : desiredIds) {
                final boolean alreadyHas = task.getDependencies().stream()
                        .anyMatch(d -> d != null && desiredId.equals(d.getId()));
                if (alreadyHas) {
                    continue;
                }
                final Task dependency = taskRepository.findById(desiredId)
                        .orElseThrow(() -> new DomainException("Зависимость не найдена", "DEPENDENCY_NOT_FOUND"));
                task.addDependency(dependency);
            }
        }
        return taskRepository.update(task);
    }
}