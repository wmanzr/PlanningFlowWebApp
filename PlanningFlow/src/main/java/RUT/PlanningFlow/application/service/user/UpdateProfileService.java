package RUT.PlanningFlow.application.service.user;

import RUT.PlanningFlow.application.port.in.user.UpdateProfileUseCase;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UpdateProfileService implements UpdateProfileUseCase {

    private final UserRepositoryPort userRepository;

    public UpdateProfileService(final UserRepositoryPort userRepository) {
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Integer> execute(final Integer userId, final String fullName) {
        DomainAssert.notNull(userId, "ID пользователя обязателен", "USER_ID_REQUIRED");
        final Optional<User> exUser = userRepository.findById(userId);
        if (exUser.isEmpty()) {
            return Optional.empty();
        }

        final User user = exUser.get();
        if (fullName != null) {
            user.rename(fullName);
        }

        return userRepository.update(user);
    }
}

