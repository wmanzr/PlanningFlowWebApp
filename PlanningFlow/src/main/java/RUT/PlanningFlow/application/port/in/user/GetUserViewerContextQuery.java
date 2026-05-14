package RUT.PlanningFlow.application.port.in.user;

import RUT.PlanningFlow.application.dto.user.UserViewerContextDto;

public interface GetUserViewerContextQuery {
    UserViewerContextDto execute(int viewerUserId, int targetUserId);
}
