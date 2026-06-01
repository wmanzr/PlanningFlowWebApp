package RUT.PlanningFlow.application.service.user;

import RUT.PlanningFlow.application.dto.user.UserSkillResponseDto;
import RUT.PlanningFlow.application.port.in.user.GetUserSkillsQuery;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.model.UserSkill;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetUserSkillsService implements GetUserSkillsQuery {

    private final UserRepositoryPort userRepository;
    private final UserSkillResponseDtoMapper userSkillResponseDtoMapper;

    public GetUserSkillsService(
            final UserRepositoryPort userRepository,
            final UserSkillResponseDtoMapper userSkillResponseDtoMapper
    ) {
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userSkillResponseDtoMapper, "Маппер ответа по навыку пользователя обязателен", "USER_SKILL_RESPONSE_DTO_MAPPER_REQUIRED");
        this.userRepository = userRepository;
        this.userSkillResponseDtoMapper = userSkillResponseDtoMapper;
    }

    @Override
    public List<UserSkillResponseDto> execute(final Integer userId) {
        DomainAssert.notNull(userId, "ID пользователя обязателен", "USER_ID_REQUIRED");
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("Пользователь не найден", "USER_NOT_FOUND"));
        if (PlanningAccessPolicy.hasRole(user, UserRoles.COORDINATOR)
                || PlanningAccessPolicy.hasRole(user, UserRoles.ORGANIZER)) {
            return List.of();
        }
        final List<UserSkill> skills = userRepository.findSkillsForUser(userId);
        final List<UserSkillResponseDto> items = new ArrayList<>(skills.size());
        for (final UserSkill us : skills) {
            items.add(userSkillResponseDtoMapper.toResponse(us));
        }
        return List.copyOf(items);
    }
}