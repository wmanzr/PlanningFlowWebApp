package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InternalResourceTest {

    @Test
    void mark_broken_then_operational() {
        final InternalResource resource = new InternalResource(1, "Van", ResourceType.TRANSPORT, "INV-1");

        resource.markBroken();
        assertThat(resource.isOperational()).isFalse();

        resource.markOperational();
        assertThat(resource.isOperational()).isTrue();
    }

    @Test
    void rename_and_change_type_delegate_to_resource() {
        final InternalResource resource = new InternalResource(1, "Van", ResourceType.TRANSPORT, "INV-1");

        resource.rename("Truck");
        resource.changeType(ResourceType.EQUIPMENT);

        assertThat(resource.getName()).isEqualTo("Truck");
        assertThat(resource.getType()).isEqualTo(ResourceType.EQUIPMENT);
    }

    @Test
    void blank_inventory_on_construct() {
        assertThatThrownBy(() -> new InternalResource(1, "Van", ResourceType.TRANSPORT, " "))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVENTORY_NUMBER_REQUIRED");
    }
}
