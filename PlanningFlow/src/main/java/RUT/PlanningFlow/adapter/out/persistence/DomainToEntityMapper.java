package RUT.PlanningFlow.adapter.out.persistence;

import RUT.PlanningFlow.adapter.out.persistence.entity.EventEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.RoleEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.SkillEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.TaskEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.UserEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.AssignmentEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.IncidentEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.UserSkillEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.ResourceEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.InternalResourceEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.ExternalResourceEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.ResourceBookingEntity;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Role;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.model.Assignment;
import RUT.PlanningFlow.domain.model.Incident;
import RUT.PlanningFlow.domain.model.UserSkill;
import RUT.PlanningFlow.domain.model.Resource;
import RUT.PlanningFlow.domain.model.InternalResource;
import RUT.PlanningFlow.domain.model.ExternalResource;
import RUT.PlanningFlow.domain.model.ResourceBooking;
import RUT.PlanningFlow.domain.vo.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class DomainToEntityMapper {
    private DomainToEntityMapper() {
    }

    static TaskEntity toEntity(final Task task) {
        if (task == null) {
            return null;
        }
        final TaskEntity e = new TaskEntity();
        applyToEntity(task, e);
        return e;
    }

    static void applyToEntity(final Task task, final TaskEntity target) {
        if (task == null || target == null) {
            return;
        }

        if (task.getEvent() != null && task.getEvent().getId() != null) {
            final EventEntity eventStub = new EventEntity();
            eventStub.setId(task.getEvent().getId());
            target.setEvent(eventStub);
        } else {
            target.setEvent(null);
        }

        if (task.getCreatedBy() != null && task.getCreatedBy().getId() != null) {
            final UserEntity createdByStub = new UserEntity();
            createdByStub.setId(task.getCreatedBy().getId());
            target.setCreatedBy(createdByStub);
        } else {
            target.setCreatedBy(null);
        }

        target.setTitle(task.getTitle());
        target.setStatus(task.getStatus());
        target.setStartTime(task.getStartTime());
        target.setEndTime(task.getEndTime());

        final GeoPoint location = task.getLocation();
        target.setLatitude(location == null ? null : location.getLatitude());
        target.setLongitude(location == null ? null : location.getLongitude());

        final List<SkillEntity> skillStubs = new ArrayList<>();
        if (task.getRequiredSkills() != null) {
            for (final Skill s : task.getRequiredSkills()) {
                if (s != null && s.getId() != null) {
                    final SkillEntity skillStub = new SkillEntity();
                    skillStub.setId(s.getId());
                    skillStubs.add(skillStub);
                }
            }
        }
        target.setRequiredSkills(skillStubs);

        final List<TaskEntity> depStubs = new ArrayList<>();
        if (task.getDependencies() != null) {
            for (final Task d : task.getDependencies()) {
                if (d != null && d.getId() != null) {
                    final TaskEntity taskStub = new TaskEntity();
                    taskStub.setId(d.getId());
                    depStubs.add(taskStub);
                }
            }
        }
        target.setDependencies(depStubs);
    }

    static EventEntity toEntity(final Event event) {
        if (event == null) {
            return null;
        }
        final EventEntity e = new EventEntity();
        applyToEntity(event, e);
        return e;
    }

    static void applyToEntity(final Event event, final EventEntity target) {
        if (event == null || target == null) {
            return;
        }

        target.setTitle(event.getTitle());
        target.setDescription(event.getDescription());
        target.setStatus(event.getStatus());
        target.setStartDate(event.getStartDate());
        target.setEndDate(event.getEndDate());

        final GeoPoint location = event.getLocation();
        target.setLatitude(location == null ? null : location.getLatitude());
        target.setLongitude(location == null ? null : location.getLongitude());

        if (event.getCreator() != null && event.getCreator().getId() != null) {
            final UserEntity creatorStub = new UserEntity();
            creatorStub.setId(event.getCreator().getId());
            target.setCreator(creatorStub);
        } else {
            target.setCreator(null);
        }

        final List<UserEntity> coordinatorStubs = new ArrayList<>();
        final List<User> coordinators = event.getCoordinators();
        if (coordinators != null) {
            for (final User u : coordinators) {
                if (u == null || u.getId() == null) {
                    continue;
                }
                final UserEntity stub = new UserEntity();
                stub.setId(u.getId());
                coordinatorStubs.add(stub);
            }
        }
        target.setCoordinators(coordinatorStubs);
    }

    static UserEntity toEntity(final User user) {
        if (user == null) {
            return null;
        }
        final UserEntity e = new UserEntity();
        applyToEntity(user, e);
        return e;
    }

    static void applyToEntity(final User user, final UserEntity target) {
        if (user == null || target == null) {
            return;
        }

        target.setUsername(user.getUsername());
        target.setPassword(user.getPassword());
        target.setEmail(user.getEmail());
        target.setFullName(user.getFullName());
        target.setBirthDate(user.getBirthDate());

        final List<RoleEntity> roleStubs = new ArrayList<>();
        final List<Role> roles = user.getRoles();
        if (roles != null) {
            for (final Role r : roles) {
                if (r == null) {
                    continue;
                }
                if (r.getId() != null) {
                    final RoleEntity stub = new RoleEntity();
                    stub.setId(r.getId());
                    stub.setName(r.getName());
                    roleStubs.add(stub);
                }
            }
        }
        target.setRoles(roleStubs);
    }

    static AssignmentEntity toEntity(final Assignment assignment) {
        if (assignment == null) {
            return null;
        }
        final AssignmentEntity e = new AssignmentEntity();
        applyToEntity(assignment, e);
        return e;
    }

    static void applyToEntity(final Assignment assignment, final AssignmentEntity target) {
        if (assignment == null || target == null) {
            return;
        }

        if (assignment.getTask() != null && assignment.getTask().getId() != null) {
            final TaskEntity taskStub = new TaskEntity();
            taskStub.setId(assignment.getTask().getId());
            target.setTask(taskStub);
        } else {
            target.setTask(null);
        }

        if (assignment.getUser() != null && assignment.getUser().getId() != null) {
            final UserEntity userStub = new UserEntity();
            userStub.setId(assignment.getUser().getId());
            target.setUser(userStub);
        } else {
            target.setUser(null);
        }

        target.setStatus(assignment.getStatus());
        target.setAssignedAt(assignment.getAssignedAt());
        target.setRespondedAt(assignment.getRespondedAt());
        target.setRejectionReason(assignment.getRejectionReason());
    }

    static IncidentEntity toEntity(final Incident incident) {
        if (incident == null) {
            return null;
        }
        final IncidentEntity e = new IncidentEntity();
        applyToEntity(incident, e);
        return e;
    }

    static void applyToEntity(final Incident incident, final IncidentEntity target) {
        if (incident == null || target == null) {
            return;
        }

        if (incident.getEvent() != null && incident.getEvent().getId() != null) {
            final EventEntity eventStub = new EventEntity();
            eventStub.setId(incident.getEvent().getId());
            target.setEvent(eventStub);
        } else {
            target.setEvent(null);
        }

        if (incident.getTask() != null && incident.getTask().getId() != null) {
            final TaskEntity taskStub = new TaskEntity();
            taskStub.setId(incident.getTask().getId());
            target.setTask(taskStub);
        } else {
            target.setTask(null);
        }

        if (incident.getResource() != null && incident.getResource().getId() != null) {
            final Resource domainResource = incident.getResource();
            final ResourceEntity resourceStub;
            if (domainResource instanceof InternalResource) {
                resourceStub = new InternalResourceEntity();
            } else if (domainResource instanceof ExternalResource) {
                resourceStub = new ExternalResourceEntity();
            } else {
                throw new IllegalArgumentException("Unknown resource domain type: " + domainResource.getClass());
            }
            resourceStub.setId(domainResource.getId());
            target.setResource(resourceStub);
        } else {
            target.setResource(null);
        }

        if (incident.getReporter() != null && incident.getReporter().getId() != null) {
            final UserEntity reporterStub = new UserEntity();
            reporterStub.setId(incident.getReporter().getId());
            target.setReporter(reporterStub);
        } else {
            target.setReporter(null);
        }

        target.setDescription(incident.getDescription());
        target.setSeverity(incident.getSeverity());
        target.setStatus(incident.getStatus());
        target.setCreatedAt(incident.getCreatedAt());
        target.setResolvedAt(incident.getResolvedAt());
        target.setResolutionNotes(incident.getResolutionNotes());
    }

    static RoleEntity toEntity(final Role role) {
        if (role == null) {
            return null;
        }
        final RoleEntity e = new RoleEntity();
        applyToEntity(role, e);
        return e;
    }

    static void applyToEntity(final Role role, final RoleEntity target) {
        if (role == null || target == null) {
            return;
        }
        target.setName(role.getName());
    }

    static SkillEntity toEntity(final Skill skill) {
        if (skill == null) {
            return null;
        }
        final SkillEntity e = new SkillEntity();
        applyToEntity(skill, e);
        return e;
    }

    static void applyToEntity(final Skill skill, final SkillEntity target) {
        if (skill == null || target == null) {
            return;
        }
        target.setName(skill.getName());
        target.setCategory(skill.getCategory());
    }

    static UserSkillEntity toEntity(final UserSkill userSkill) {
        if (userSkill == null) {
            return null;
        }
        final UserSkillEntity e = new UserSkillEntity();
        applyToEntity(userSkill, e);
        return e;
    }

    static void applyToEntity(final UserSkill userSkill, final UserSkillEntity target) {
        if (userSkill == null || target == null) {
            return;
        }

        if (userSkill.getUser() != null && userSkill.getUser().getId() != null) {
            final UserEntity userStub = new UserEntity();
            userStub.setId(userSkill.getUser().getId());
            target.setUser(userStub);
        } else {
            target.setUser(null);
        }

        if (userSkill.getSkill() != null && userSkill.getSkill().getId() != null) {
            final SkillEntity skillStub = new SkillEntity();
            skillStub.setId(userSkill.getSkill().getId());
            target.setSkill(skillStub);
        } else {
            target.setSkill(null);
        }

        target.setTier(userSkill.getTier());
        target.setVerifiedAt(userSkill.getVerifiedAt());
    }

    static ResourceEntity toEntity(final Resource resource) {
        if (resource == null) {
            return null;
        }
        if (resource instanceof InternalResource ir) {
            return toEntity(ir);
        }
        if (resource instanceof ExternalResource er) {
            return toEntity(er);
        }
        throw new IllegalArgumentException("Unknown resource domain type: " + resource.getClass());
    }

    static void applyToEntity(final Resource resource, final ResourceEntity target) {
        if (resource == null || target == null) {
            return;
        }
        target.setName(resource.getName());
        target.setType(resource.getType());
        target.setOperational(resource.isOperational());
    }

    static InternalResourceEntity toEntity(final InternalResource resource) {
        if (resource == null) {
            return null;
        }
        final InternalResourceEntity e = new InternalResourceEntity();
        applyToEntity(resource, e);
        return e;
    }

    static void applyToEntity(final InternalResource resource, final InternalResourceEntity target) {
        if (resource == null || target == null) {
            return;
        }
        applyToEntity((Resource) resource, (ResourceEntity) target);
        target.setInventoryNumber(resource.getInventoryNumber());
    }

    static ExternalResourceEntity toEntity(final ExternalResource resource) {
        if (resource == null) {
            return null;
        }
        final ExternalResourceEntity e = new ExternalResourceEntity();
        applyToEntity(resource, e);
        return e;
    }

    static void applyToEntity(final ExternalResource resource, final ExternalResourceEntity target) {
        if (resource == null || target == null) {
            return;
        }
        applyToEntity((Resource) resource, (ResourceEntity) target);
        target.setExternalApiId(resource.getExternalApiId());
    }

    static ResourceBookingEntity toEntity(final ResourceBooking booking) {
        if (booking == null) {
            return null;
        }
        final ResourceBookingEntity e = new ResourceBookingEntity();
        applyToEntity(booking, e);
        return e;
    }

    static void applyToEntity(final ResourceBooking booking, final ResourceBookingEntity target) {
        if (booking == null || target == null) {
            return;
        }
        if (booking.getTask() != null && booking.getTask().getId() != null) {
            final TaskEntity taskStub = new TaskEntity();
            taskStub.setId(booking.getTask().getId());
            target.setTask(taskStub);
        } else {
            target.setTask(null);
        }

        final Resource r = booking.getResource();
        if (r != null && r.getId() != null) {
            final ResourceEntity resourceStub;
            if (r instanceof InternalResource ir) {
                resourceStub = new InternalResourceEntity();
                resourceStub.setId(ir.getId());
            } else if (r instanceof ExternalResource er) {
                resourceStub = new ExternalResourceEntity();
                resourceStub.setId(er.getId());
            } else {
                throw new IllegalArgumentException("Unknown resource type: " + r.getClass());
            }
            target.setResource(resourceStub);
        } else {
            target.setResource(null);
        }

        target.setStatus(booking.getStatus());
        target.setReservedFrom(booking.getReservedFrom());
        target.setReservedTo(booking.getReservedTo());
    }
}