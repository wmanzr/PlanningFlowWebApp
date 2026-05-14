package RUT.PlanningFlow.application.port.in.notification;

public interface UnreadNotificationsCountQuery {
    long execute(Integer callerUserId);
}

