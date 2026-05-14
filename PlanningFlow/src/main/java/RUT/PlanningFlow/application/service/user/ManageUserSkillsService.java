package RUT.PlanningFlow.application.service.user;

import RUT.PlanningFlow.application.port.in.user.ManageUserSkillsUseCase;
import RUT.PlanningFlow.application.port.out.repository.SkillRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserRepositoryPort;
import RUT.PlanningFlow.application.port.out.repository.UserSkillRepositoryPort;
import RUT.PlanningFlow.application.security.PlanningAccessPolicy;
import RUT.PlanningFlow.domain.enums.SkillTier;
import RUT.PlanningFlow.domain.enums.UserRoles;
import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.model.Skill;
import RUT.PlanningFlow.domain.model.User;
import RUT.PlanningFlow.domain.model.UserSkill;
import RUT.PlanningFlow.domain.utils.DomainAssert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class ManageUserSkillsService implements ManageUserSkillsUseCase {

    private final UserRepositoryPort userRepository;
    private final SkillRepositoryPort skillRepository;
    private final UserSkillRepositoryPort userSkillRepository;

    public ManageUserSkillsService(
            final UserRepositoryPort userRepository,
            final SkillRepositoryPort skillRepository,
            final UserSkillRepositoryPort userSkillRepository
    ) {
        DomainAssert.notNull(userRepository, "Репозиторий пользователей обязателен", "USER_REPOSITORY_REQUIRED");
        DomainAssert.notNull(skillRepository, "Репозиторий навыков обязателен", "SKILL_REPOSITORY_REQUIRED");
        DomainAssert.notNull(userSkillRepository, "Репозиторий навыков пользователя обязателен", "USER_SKILL_REPOSITORY_REQUIRED");
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
    }

    @Override
    public List<Integer> execute(final Integer userId, final Map<Integer, SkillTier> skillTiers) {
        DomainAssert.notNull(userId, "ID пользователя обязателен", "USER_ID_REQUIRED");
        DomainAssert.notNull(skillTiers, "Список навыков обязателен", "USER_SKILLS_REQUIRED");

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("Пользователь не найден", "USER_NOT_FOUND"));
        if (PlanningAccessPolicy.hasRole(user, UserRoles.COORDINATOR)
                || PlanningAccessPolicy.hasRole(user, UserRoles.ORGANIZER)) {
            throw new DomainException(
                    "Навыки недоступны для координаторов и организаторов",
                    "USER_SKILLS_NOT_APPLICABLE"
            );
        }

        for (final Map.Entry<Integer, SkillTier> e : skillTiers.entrySet()) {
            DomainAssert.notNull(e.getKey(), "ID навыка обязателен", "SKILL_ID_REQUIRED");
            DomainAssert.notNull(e.getValue(), "Tier обязателен", "SKILL_TIER_REQUIRED");
        }

        final List<Integer> createdIds = new ArrayList<>();

        final List<UserSkill> existing = userSkillRepository.findForUser(userId);
        final Map<Integer, UserSkill> existingBySkillId = new HashMap<>();
        for (final UserSkill us : existing) {
            if (us == null || us.getSkill() == null || us.getSkill().getId() == null) {
                continue;
            }
            existingBySkillId.put(us.getSkill().getId(), us);
        }

        for (final Map.Entry<Integer, SkillTier> e : skillTiers.entrySet()) {
            final Integer skillId = e.getKey();
            final SkillTier tier = e.getValue();

            final UserSkill exUserSkill = existingBySkillId.get(skillId);
            if (exUserSkill != null) {
                if (exUserSkill.getTier() != tier) {
                    exUserSkill.updateTier(tier);
                    userSkillRepository.update(exUserSkill);
                }
                continue;
            }

            final Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new DomainException("Навык не найден", "SKILL_NOT_FOUND"));
            userSkillRepository.create(new UserSkill(null, user, skill, tier, LocalDateTime.now()))
                    .ifPresent(createdIds::add);
        }

        final Set<Integer> desiredSkillIds = new HashSet<>(skillTiers.keySet());
        for (final UserSkill us : existing) {
            if (us == null || us.getSkill() == null || us.getSkill().getId() == null) {
                continue;
            }
            if (!desiredSkillIds.contains(us.getSkill().getId()) && us.getId() != null) {
                userSkillRepository.deleteById(us.getId());
            }
        }

        return List.copyOf(createdIds);
    }
}