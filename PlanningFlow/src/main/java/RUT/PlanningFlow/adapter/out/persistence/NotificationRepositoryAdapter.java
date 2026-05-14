package RUT.PlanningFlow.adapter.out.persistence;

import jakarta.persistence.EntityManager;

import RUT.PlanningFlow.adapter.out.persistence.entity.NotificationEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.UserEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.NotificationRepository;
import RUT.PlanningFlow.application.dto.notification.NotificationDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.out.repository.NotificationRepositoryPort;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final NotificationRepository repository;
    private final EntityManager entityManager;

    public NotificationRepositoryAdapter(
            final NotificationRepository repository,
            final EntityManager entityManager
    ) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public PageResult<NotificationDto> list(final Integer userId, final String filter, final PageQuery pageQuery) {
        DomainAssert.notNull(pageQuery, "Параметры пагинации обязательны", "PAGE_QUERY_REQUIRED");
        if (userId == null) {
            return new PageResult<>(List.of(), 0, 0);
        }
        final PageRequest pageable = PageRequest.of(pageQuery.zeroBasedPage(), pageQuery.size());
        final String f = filter == null ? "all" : filter.trim().toLowerCase();
        final Page<NotificationEntity> page = switch (f) {
            case "unread" -> repository.findUnreadByUser(userId, pageable);
            case "read" -> repository.findReadByUser(userId, pageable);
            default -> repository.findByUser(userId, pageable);
        };

        final List<NotificationDto> items = new ArrayList<>(page.getContent().size());
        for (final NotificationEntity n : page.getContent()) {
            items.add(toDto(n));
        }
        return new PageResult<>(items, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public long countUnread(final Integer userId) {
        if (userId == null) return 0;
        return repository.countUnread(userId);
    }

    @Override
    public void markRead(final Integer userId, final Integer notificationId) {
        if (userId == null || notificationId == null) return;
        repository.markRead(userId, notificationId);
    }

    @Override
    public void markAllRead(final Integer userId) {
        if (userId == null) return;
        repository.markAllRead(userId);
    }

    @Override
    public Integer create(final Integer userId, final String title, final String message, final LocalDateTime createdAt) {
        if (userId == null || message == null || message.isBlank()) {
            return null;
        }
        final NotificationEntity e = new NotificationEntity();
        e.setUser(entityManager.getReference(UserEntity.class, userId));
        e.setTitle(title == null || title.isBlank() ? null : title.trim());
        e.setMessage(message.trim());
        e.setCreatedAt(createdAt == null ? LocalDateTime.now() : createdAt);
        e.setReadAt(null);
        return repository.save(e).getId();
    }

    private static NotificationDto toDto(final NotificationEntity n) {
        if (n == null) return null;
        return new NotificationDto(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getCreatedAt(),
                n.getReadAt()
        );
    }
}

