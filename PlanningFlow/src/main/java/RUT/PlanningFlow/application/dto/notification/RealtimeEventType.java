package RUT.PlanningFlow.application.dto.notification;

public enum RealtimeEventType {
    ASSIGNMENT_ASSIGNED("assignment.assigned"),
    ASSIGNMENT_REMOVED("assignment.removed"),
    ASSIGNMENT_ACCEPTED("assignment.accepted"),
    ASSIGNMENT_REJECTED("assignment.rejected"),
    INCIDENT_REPORTED("incident.reported"),
    COORDINATOR_ASSIGNED("coordinator.assigned");

    private final String wireValue;

    RealtimeEventType(final String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }
}