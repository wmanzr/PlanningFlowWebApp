package RUT.PlanningFlow.application.service.task;

import RUT.PlanningFlow.application.port.in.task.CreateTaskUseCase;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.SkillRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CreateTaskService implements CreateTaskUseCase {

    private final EventRepositoryPort eventRepository;
    private final UserRepositoryPort userRepository;
    private final TaskRepositoryPort taskRepository;
    private final SkillRepositoryPort skillRepository;

    public CreateTaskService(
            final EventRepositoryPort eventRepository,
            final UserRepositoryPort userRepository,
            final TaskRepositoryPort taskRepository,
            final SkillRepositoryPort skillRepository
    ) {
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        DomainAssert.notNull(skillRepository, "Репозиторий навыков обязателен", "SKILL_REPOSITORY_REQUIRED");
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.skillRepository = skillRepository;
    }

    @Override
    public Integer execute(
            final Integer callerUserId,
            final Integer eventId,
            final String title,
            final LocalDateTime startTime,
            final LocalDateTime endTime,
            final GeoPoint location,
            final List<Integer> requiredSkillIds
    ) {
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");
        DomainAssert.notNull(eventId, "ID мероприятия обязателен", "EVENT_ID_REQUIRED");
        final User actor = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new DomainException("Мероприятие не найдено", "EVENT_NOT_FOUND"));
        PlanningAccessPolicy.assertCanManageEvent(actor, event);
        event.assertAllowsPlannerContentMutations();

        LocalDateTime effectiveStart = startTime != null ? startTime : event.getStartDate();
        LocalDateTime effectiveEnd = endTime != null ? endTime : event.getEndDate();
        if (startTime == null && endTime == null) {
            final LocalDateTime maxEndByPolicy = effectiveStart.plusHours(8);
            if (effectiveEnd.isAfter(maxEndByPolicy)) {
                effectiveEnd = maxEndByPolicy;
            }
        }
        if (effectiveStart.isBefore(event.getStartDate()) || effectiveEnd.isAfter(event.getEndDate())) {
            throw new DomainException("Время задачи выходит за временные рамки мероприятия", "TASK_OUT_OF_EVENT_RANGE");
        }

        Task.assertScheduleDurationAllowed(effectiveStart, effectiveEnd);

        final List<Skill> skills = resolveRequiredSkills(requiredSkillIds);

        final String trimmedTitle = title == null ? "" : title.trim();
        if (taskRepository.existsByEventIdAndTitleIgnoreCase(eventId, trimmedTitle)) {
            throw new DomainException(
                    "На этом мероприятии уже есть задача с таким названием",
                    "TASK_DUPLICATE_TITLE_IN_EVENT"
            );
        }

        final long tasksBeforeCreate = taskRepository.countTasksForEvent(eventId);

        final Task task = new Task(
                null,
                event,
                actor,
                trimmedTitle,
                TaskStatus.OPEN,
                effectiveStart,
                effectiveEnd,
                location,
                skills,
                List.of()
        );

        final Integer createdId = taskRepository.create(task)
                .orElseThrow(() -> new DomainException("Не удалось создать задачу", "TASK_CREATE_FAILED"));

        if (tasksBeforeCreate == 0 && event.getStatus() == EventStatus.DRAFT) {
            event.startPlanning();
            eventRepository.update(event);
        }

        return createdId;
    }

    private List<Skill> resolveRequiredSkills(final List<Integer> requiredSkillIds) {
        if (requiredSkillIds == null || requiredSkillIds.isEmpty()) {
            return List.of();
        }
        final Set<Integer> uniqueOrdered = new LinkedHashSet<>();
        for (final Integer id : requiredSkillIds) {
            if (id != null) {
                uniqueOrdered.add(id);
            }
        }
        final List<Skill> skills = new ArrayList<>(uniqueOrdered.size());
        for (final Integer skillId : uniqueOrdered) {
            final Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new DomainException("Навык не найден", "SKILL_NOT_FOUND"));
            skills.add(skill);
        }
        return skills;
    }
}
