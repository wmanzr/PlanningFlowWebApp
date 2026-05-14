package RUT.PlanningFlow.config;

import RUT.PlanningFlow.adapter.out.persistence.entity.RoleEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.RoleRepository;
import RUT.PlanningFlow.domain.enums.UserRoles;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class RoleCatalogBootstrap implements ApplicationRunner {

    private final RoleRepository roleRepository;

    public RoleCatalogBootstrap(final RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(final ApplicationArguments args) {
        for (final UserRoles name : UserRoles.values()) {
            if (roleRepository.findByName(name).isEmpty()) {
                roleRepository.save(new RoleEntity(name));
            }
        }
    }
}
