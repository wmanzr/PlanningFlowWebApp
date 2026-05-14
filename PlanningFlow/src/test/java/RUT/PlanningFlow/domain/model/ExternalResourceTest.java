package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.enums.ResourceType;
import RUT.PlanningFlow.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalResourceTest {

    @Test
    void update_external_api_id() {
        final ExternalResource resource = new ExternalResource(1, "RentCo", ResourceType.TRANSPORT, "api-1");

        resource.updateExternalApiId("api-2");

        assertThat(resource.getExternalApiId()).isEqualTo("api-2");
    }

    @Test
    void blank_external_api_id_rejected() {
        assertThatThrownBy(() -> new ExternalResource(1, "Vendor", ResourceType.MATERIAL, " \t"))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "EXTERNAL_API_ID_REQUIRED");
    }
}
