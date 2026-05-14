package RUT.PlanningFlow.application.service.auth;

import RUT.PlanningFlow.application.port.in.auth.RegisterUserUseCase;
import RUT.PlanningFlow.application.port.in.dto.AuthTokenResponse;
import RUT.PlanningFlow.application.port.out.auth.AuthenticationTokensIssuer;
import RUT.PlanningFlow.application.port.out.repository.RoleRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Role;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class RegisterUserService implements RegisterUserUseCase {

    private static final Set<UserRoles> ALLOWED_SELF_REGISTRATION = EnumSet.of(
            UserRoles.ORGANIZER,
            UserRoles.COORDINATOR,
            UserRoles.PARTICIPANT
    );

    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationTokensIssuer tokens;

    public RegisterUserService(
            final UserRepositoryPort userRepository,
            final RoleRepositoryPort roleRepository,
            final PasswordEncoder passwordEncoder,
            final AuthenticationTokensIssuer tokens
    ) {
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(roleRepository, "Репозиторий ролей обязателен", "ROLE_REPOSITORY_REQUIRED");
        DomainAssert.notNull(passwordEncoder, "PasswordEncoder обязателен", "PASSWORD_ENCODER_REQUIRED");
        DomainAssert.notNull(tokens, "Выдача токенов обязательна", "TOKENS_ISSUER_REQUIRED");
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Override
    public AuthTokenResponse register(
            final String username,
            final String rawPassword,
            final String email,
            final String fullName,
            final LocalDate birthDate,
            final UserRoles role
    ) {
        DomainAssert.notNull(role, "Роль при регистрации обязательна", "REGISTRATION_ROLE_REQUIRED");

        if (!ALLOWED_SELF_REGISTRATION.contains(role)) {
            throw new DomainException(
                    "Можно выбрать только роли организатора, координатора или участника",
                    "REGISTRATION_ROLE_NOT_ALLOWED"
            );
        }

        final String usernameNorm = username == null ? "" : username.trim();
        final String emailNorm = email == null ? "" : email.trim();
        final String fullNameNorm = fullName == null ? "" : fullName.trim();
        DomainAssert.notBlank(usernameNorm, "Имя пользователя обязательно", "USERNAME_REQUIRED");
        DomainAssert.notBlank(emailNorm, "Email обязателен", "EMAIL_REQUIRED");
        DomainAssert.notBlank(fullNameNorm, "ФИО обязательно", "FULL_NAME_REQUIRED");
        DomainAssert.notBlank(rawPassword, "Пароль обязателен", "PASSWORD_REQUIRED");

        if (userRepository.findByUsername(usernameNorm).isPresent()) {
            throw new DomainException("Пользователь с таким именем уже есть", "USERNAME_TAKEN");
        }
        if (userRepository.findByEmailIgnoreCase(emailNorm).isPresent()) {
            throw new DomainException("Пользователь с таким email уже есть", "EMAIL_TAKEN");
        }

        final Role roleEntity = roleRepository.findByName(role)
                .orElseThrow(() -> new DomainException(
                        "Роль недоступна (нет записи в каталоге ролей)",
                        "ROLE_NOT_FOUND"
                ));

        final String encodedPassword = passwordEncoder.encode(rawPassword);
        final User toCreate = new User(
                null,
                usernameNorm,
                encodedPassword,
                emailNorm,
                fullNameNorm,
                birthDate,
                List.of(roleEntity)
        );

        final Integer userId = userRepository.create(toCreate)
                .orElseThrow(() -> new DomainException("Не удалось создать пользователя", "USER_CREATE_FAILED"));

        final User saved = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("Не удалось загрузить созданного пользователя", "USER_LOAD_FAILED"));

        return tokens.issueAfterLogin(saved);
    }
}