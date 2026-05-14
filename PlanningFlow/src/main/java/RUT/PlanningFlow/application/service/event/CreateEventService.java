package RUT.PlanningFlow.application.service.event;

import RUT.PlanningFlow.application.port.in.event.CreateEventUseCase;
import RUT.PlanningFlow.application.port.out.repository.EventRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.domain.enums.EventStatus;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CreateEventService implements CreateEventUseCase {

    private final EventRepositoryPort eventRepository;
    private final UserRepositoryPort userRepository;

    public CreateEventService(
            final EventRepositoryPort eventRepository,
            final UserRepositoryPort userRepository
    ) {
        DomainAssert.notNull(eventRepository, "Репозиторий мероприятий обязателен", "EVENT_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Integer execute(
            final String title,
            final String description,
            final LocalDateTime startDate,
            final LocalDateTime endDate,
            final GeoPoint location,
            final Integer creatorUserId
    ) {
        DomainAssert.notNull(creatorUserId, "Создатель мероприятия обязателен", "EVENT_CREATOR_REQUIRED");

        final User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new DomainException("Создатель мероприятия не найден", "EVENT_CREATOR_NOT_FOUND"));

        final Event event = new Event(
                null,
                title,
                description,
                EventStatus.DRAFT,
                startDate,
                endDate,
                location,
                creator,
                List.of()
        );

        return eventRepository.create(event)
                .orElseThrow(() -> new DomainException("Не удалось создать мероприятие", "EVENT_CREATE_FAILED"));
    }
}