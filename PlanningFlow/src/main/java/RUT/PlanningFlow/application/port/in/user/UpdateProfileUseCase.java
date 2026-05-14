package RUT.PlanningFlow.application.port.in.user;

import java.util.Optional;

public interface UpdateProfileUseCase {
    Optional<Integer> execute(Integer userId, String fullName);
}
