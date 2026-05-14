package RUT.PlanningFlow.application.port.out;

import RUT.PlanningFlow.application.dto.notification.RealtimeMessage;

import java.util.Collection;

public interface NotificationPublisherPort {
    void publishToUser(int userId, RealtimeMessage message);
    void publishToUsers(Collection<Integer> userIds, RealtimeMessage message);
}