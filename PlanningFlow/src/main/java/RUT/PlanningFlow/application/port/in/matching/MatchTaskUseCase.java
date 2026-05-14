package RUT.PlanningFlow.application.port.in.matching;

import RUT.PlanningFlow.application.dto.matching.MatchTaskResponseDto;
import RUT.PlanningFlow.domain.vo.EventMode;

public interface MatchTaskUseCase {
    MatchTaskResponseDto execute(Integer callerUserId, Integer taskId, EventMode mode, int requiredCount);
}
