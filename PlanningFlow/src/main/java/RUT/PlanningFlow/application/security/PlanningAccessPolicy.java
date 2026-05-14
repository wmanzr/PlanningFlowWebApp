package RUT.PlanningFlow.application.security;

import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.model.Event;
import RUT.PlanningFlow.domain.model.Role;
import RUT.PlanningFlow.domain.model.Task;
import RUT.PlanningFlow.domain.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class PlanningAccessPolicy {

    private PlanningAccessPolicy() {
    }

    public static boolean hasRole(final User user, final UserRoles role) {
        if (user == null || role == null) {
            return false;
        }
        for (final Role r : user.getRoles()) {
            if (r != null && role.equals(r.getName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean canViewEvent(final User actor, final Event event, final boolean participantAssignedToEvent) {
        if (actor == null || event == null) {
            return false;
        }
        if (hasRole(actor, UserRoles.ADMIN)) {
            return true;
        }
        if (canManageEvent(actor, event)) {
            return true;
        }
        return hasRole(actor, UserRoles.PARTICIPANT) && participantAssignedToEvent;
    }

    public static boolean canManageEvent(final User actor, final Event event) {
        if (actor == null || event == null || actor.getId() == null) {
            return false;
        }
        if (hasRole(actor, UserRoles.ADMIN)) {
            return true;
        }
        if (event.getCreator() != null && actor.getId().equals(event.getCreator().getId())) {
            return true;
        }
        if (event.getCoordinators() != null) {
            for (final User c : event.getCoordinators()) {
                if (c != null && actor.getId().equals(c.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void assertCanManageEvent(final User actor, final Event event) {
        if (!canManageEvent(actor, event)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    
    public static boolean canEditEvent(final User actor, final Event event) {
        if (actor == null || event == null || actor.getId() == null) {
            return false;
        }
        if (hasRole(actor, UserRoles.ADMIN)) {
            return true;
        }
        return event.getCreator() != null && actor.getId().equals(event.getCreator().getId());
    }

    public static void assertCanEditEvent(final User actor, final Event event) {
        if (!canEditEvent(actor, event)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    public static void assertCanManageTaskAsPlanner(final User actor, final Task task) {
        if (task == null || task.getEvent() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        assertCanManageEvent(actor, task.getEvent());
    }

    public static boolean canViewTask(final User actor, final Task task, final boolean assignedToThisTask) {
        if (actor == null || task == null) {
            return false;
        }
        if (hasRole(actor, UserRoles.ADMIN)) {
            return true;
        }
        if (task.getEvent() != null && canManageEvent(actor, task.getEvent())) {
            return true;
        }
        return hasRole(actor, UserRoles.PARTICIPANT) && assignedToThisTask;
    }

    public static void assertCanViewTask(final User actor, final Task task, final boolean assignedToThisTask) {
        if (!canViewTask(actor, task, assignedToThisTask)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
