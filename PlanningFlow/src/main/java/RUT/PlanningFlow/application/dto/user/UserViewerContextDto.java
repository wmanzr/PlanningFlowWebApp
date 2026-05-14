package RUT.PlanningFlow.application.dto.user;

import java.util.List;

public record UserViewerContextDto(
        List<UserAssignmentSnapshotDto> adminAllAssignments,
        List<UserAssignmentSnapshotDto> organizerParticipantAssignments,
        List<EventBriefViewerDto> organizerCoordinatorEvents,
        Long completedEventsAsOrganizerCount,
        Long coordinatorEventsUnderOrganizerCount,
        Long coordinatorEventsTotalCount,
        
        Long participantEventsUnderViewerCount,
        
        Long participantEventsTotalCount
) {
    public UserViewerContextDto {
        adminAllAssignments = adminAllAssignments == null ? List.of() : List.copyOf(adminAllAssignments);
        organizerParticipantAssignments =
                organizerParticipantAssignments == null ? List.of() : List.copyOf(organizerParticipantAssignments);
        organizerCoordinatorEvents =
                organizerCoordinatorEvents == null ? List.of() : List.copyOf(organizerCoordinatorEvents);
    }
}
