package RUT.PlanningFlow.application.service.event;

import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.dto.event.EventResponseDto;
import RUT.PlanningFlow.application.port.in.event.ListEventsByDateRangeQuery;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.TaskRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.model.Event;
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
public class ListEventsByDateRangeService implements ListEventsByDateRangeQuery {

    private final EventRepositoryPort eventRepository;
    private final UserRepositoryPort userRepository;
    private final TaskRepositoryPort taskRepository;

    public ListEventsByDateRangeService(
            final EventRepositoryPort eventRepository,
            final UserRepositoryPort userRepository,
            final TaskRepositoryPort taskRepository
    ) {
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(taskRepository, "Репозиторий задач обязателен", "TASK_REPOSITORY_REQUIRED");
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public PageResult<EventResponseDto> execute(
            final Integer callerUserId,
            final LocalDateTime start,
            final LocalDateTime end,
            final String title,
            final PageQuery pageQuery
    ) {
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        DomainAssert.notNull(callerUserId, "Идентификатор вызывающего пользователя обязателен", "CALLER_USER_ID_REQUIRED");

        final User caller = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        final String titlePart = (title == null || title.isBlank()) ? null : title.trim();
        final boolean hasDates = start != null && end != null;
        final boolean hasTitle = titlePart != null;

        final PageResult<Event> page;
        if (PlanningAccessPolicy.hasRole(caller, UserRoles.ADMIN)) {
            if (!hasTitle && !hasDates) {
                page = eventRepository.findEvents(pageQuery);
            } else if (!hasTitle) {
                page = eventRepository.findByStartDateBetween(start, end, pageQuery);
            } else if (!hasDates) {
                page = eventRepository.findByTitleContainingIgnoreCase(titlePart, pageQuery);
            } else {
                page = eventRepository.findByTitleContainingIgnoreCaseAndStartDateBetween(titlePart, start, end, pageQuery);
            }
        } else {
            final boolean isCoordinator = PlanningAccessPolicy.hasRole(caller, UserRoles.COORDINATOR);
            final boolean isOrganizer = PlanningAccessPolicy.hasRole(caller, UserRoles.ORGANIZER);

            if (isCoordinator) {
                if (!hasTitle && !hasDates) {
                    page = eventRepository.findByCoordinator(callerUserId, pageQuery);
                } else if (!hasTitle) {
                    page = eventRepository.findByCoordinatorAndStartDateBetween(callerUserId, start, end, pageQuery);
                } else if (!hasDates) {
                    page = eventRepository.findByCoordinatorAndTitleContainingIgnoreCase(callerUserId, titlePart, pageQuery);
                } else {
                    page = eventRepository.findByCoordinatorAndTitleContainingIgnoreCaseAndStartDateBetween(
                            callerUserId,
                            titlePart,
                            start,
                            end,
                            pageQuery
                    );
                }
            } else if (isOrganizer) {
                if (!hasTitle && !hasDates) {
                    page = eventRepository.findByCreator(callerUserId, pageQuery);
                } else if (!hasTitle) {
                    page = eventRepository.findByCreatorAndStartDateBetween(callerUserId, start, end, pageQuery);
                } else if (!hasDates) {
                    page = eventRepository.findByCreatorAndTitleContainingIgnoreCase(callerUserId, titlePart, pageQuery);
                } else {
                    page = eventRepository.findByCreatorAndTitleContainingIgnoreCaseAndStartDateBetween(
                            callerUserId,
                            titlePart,
                            start,
                            end,
                            pageQuery
                    );
                }
            } else {
                if (!hasTitle && !hasDates) {
                    page = eventRepository.findAccessibleByUser(callerUserId, pageQuery);
                } else if (!hasTitle) {
                    page = eventRepository.findAccessibleByUserAndStartDateBetween(callerUserId, start, end, pageQuery);
                } else if (!hasDates) {
                    page = eventRepository.findAccessibleByUserAndTitleContainingIgnoreCase(callerUserId, titlePart, pageQuery);
                } else {
                    page = eventRepository.findAccessibleByUserAndTitleContainingIgnoreCaseAndStartDateBetween(
                            callerUserId,
                            titlePart,
                            start,
                            end,
                            pageQuery
                    );
                }
            }
        }

        final List<EventResponseDto> items = new ArrayList<>(page.items().size());
        for (final Event e : page.items()) {
            final long tasks = taskRepository.countTasksForEvent(e.getId());
            items.add(EventResponseDto.from(e, tasks));
        }
        return new PageResult<>(items, page.totalElements(), page.totalPages());
    }
}
