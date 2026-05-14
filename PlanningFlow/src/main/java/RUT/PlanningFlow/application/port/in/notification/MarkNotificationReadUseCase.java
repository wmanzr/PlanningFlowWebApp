package RUT.PlanningFlow.application.port.in.notification;

public interface MarkNotificationReadUseCase {
    void execute(Integer callerUserId, Integer notificationId);
}

