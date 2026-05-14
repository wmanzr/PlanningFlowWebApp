package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.*;
import RUT.PlanningFlow.domain.model.Assignment;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.ExternalResource;
import RUT.PlanningFlow.domain.model.Incident;
import RUT.PlanningFlow.domain.model.InternalResource;
import RUT.PlanningFlow.domain.model.Resource;
import RUT.PlanningFlow.domain.model.ResourceBooking;
import RUT.PlanningFlow.domain.model.Role;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.model.UserSkill;
import RUT.PlanningFlow.domain.vo.GeoPoint;
import org.hibernate.proxy.HibernateProxy;

import java.util.ArrayList;
import java.util.List;

final class EntityToDomainMapper {
    private EntityToDomainMapper() {
    }

    static User toDomain(final UserEntity e) {
        if (e == null) {
            return null;
        }
        final List<Role> roles = new ArrayList<>();
        if (e.getRoles() != null) {
            for (final RoleEntity r : e.getRoles()) {
                roles.add(toDomain(r));
            }
        }
        return new User(
                e.getId(),
                e.getUsername(),
                e.getPassword(),
                e.getEmail(),
                e.getFullName(),
                e.getBirthDate(),
                roles
        );
    }

    static Role toDomain(final RoleEntity e) {
        if (e == null) {
            return null;
        }
        return new Role(e.getId(), e.getName());
    }

    static Skill toDomain(final SkillEntity e) {
        if (e == null) {
            return null;
        }
        return new Skill(e.getId(), e.getName(), e.getCategory());
    }

    static Event toDomain(final EventEntity e) {
        if (e == null) {
            return null;
        }
        final GeoPoint location = e.getLatitude() == null || e.getLongitude() == null
                ? null
                : new GeoPoint(e.getLatitude(), e.getLongitude());
        final List<User> coordinators = new ArrayList<>();
        if (e.getCoordinators() != null) {
            for (final UserEntity u : e.getCoordinators()) {
                coordinators.add(toDomain(u));
            }
        }
        return new Event(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getStatus(),
                e.getStartDate(),
                e.getEndDate(),
                location,
                toDomain(e.getCreator()),
                coordinators
        );
    }

    static Task toDomainShallow(final TaskEntity e) {
        if (e == null) {
            return null;
        }
        return mapFields(e, List.of());
    }

    static Task toDomain(final TaskEntity e) {
        if (e == null) {
            return null;
        }

        final List<Task> dependencies = new ArrayList<>();
        if (e.getDependencies() != null) {
            for (final TaskEntity depEntity : e.getDependencies()) {
                dependencies.add(toDomainShallow(depEntity));
            }
        }
        return mapFields(e, dependencies);
    }

    private static Task mapFields(final TaskEntity e, final List<Task> dependencies) {
        final GeoPoint location = e.getLatitude() == null || e.getLongitude() == null
                ? null
                : new GeoPoint(e.getLatitude(), e.getLongitude());

        final List<Skill> required = new ArrayList<>();
        if (e.getRequiredSkills() != null) {
            for (final SkillEntity s : e.getRequiredSkills()) {
                required.add(toDomain(s));
            }
        }

        return new Task(
                e.getId(),
                toDomain(e.getEvent()),
                toDomain(e.getCreatedBy()),
                e.getTitle(),
                e.getStatus(),
                e.getStartTime(),
                e.getEndTime(),
                location,
                required,
                dependencies
        );
    }

    static Assignment toDomain(final AssignmentEntity e) {
        if (e == null) {
            return null;
        }
        return new Assignment(
                e.getId(),
                toDomain(e.getTask()),
                toDomain(e.getUser()),
                e.getStatus(),
                e.getAssignedAt(),
                e.getRespondedAt(),
                e.getRejectionReason()
        );
    }

    static Incident toDomain(final IncidentEntity e) {
        if (e == null) {
            return null;
        }
        return new Incident(
                e.getId(),
                toDomain(e.getEvent()),
                toDomain(e.getTask()),
                toDomain(e.getResource()),
                toDomain(e.getReporter()),
                e.getDescription(),
                e.getSeverity(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getResolvedAt(),
                e.getResolutionNotes()
        );
    }

    static UserSkill toDomain(final UserSkillEntity e) {
        if (e == null) {
            return null;
        }
        return new UserSkill(
                e.getId(),
                toDomain(e.getUser()),
                toDomain(e.getSkill()),
                e.getTier(),
                e.getVerifiedAt()
        );
    }

    
    private static ResourceEntity materializeResourceEntity(final ResourceEntity entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof HibernateProxy proxy) {
            final Object implementation = proxy.getHibernateLazyInitializer().getImplementation();
            if (implementation instanceof ResourceEntity concrete) {
                return concrete;
            }
            throw new IllegalStateException(
                    "Resource association proxy expected ResourceEntity implementation, was: "
                            + implementation.getClass().getName()
            );
        }
        return entity;
    }

    static Resource toDomain(final ResourceEntity e) {
        final ResourceEntity concrete = materializeResourceEntity(e);
        if (concrete == null) {
            return null;
        }
        switch (concrete) {
            case InternalResourceEntity ie -> {
                final InternalResource r = new InternalResource(ie.getId(), ie.getName(), ie.getType(), ie.getInventoryNumber());
                if (!ie.isOperational()) {
                    r.markBroken();
                }
                return r;
            }
            case ExternalResourceEntity ee -> {
                final ExternalResource r = new ExternalResource(ee.getId(), ee.getName(), ee.getType(), ee.getExternalApiId());
                if (!ee.isOperational()) {
                    r.markBroken();
                }
                return r;
            }
            default -> {
            }
        }
        throw new IllegalArgumentException("Unknown resource entity type: " + concrete.getClass().getName());
    }

    static ResourceBooking toDomain(final ResourceBookingEntity e) {
        if (e == null) {
            return null;
        }
        return new ResourceBooking(
                e.getId(),
                toDomain(e.getTask()),
                toDomain(e.getResource()),
                e.getStatus(),
                e.getReservedFrom(),
                e.getReservedTo()
        );
    }
}