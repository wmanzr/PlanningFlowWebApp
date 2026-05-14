package RUT.PlanningFlow.application.dto.notification;

public record RealtimeMessage(
        String type,
        long timestamp,
        String payloadJson
) {}