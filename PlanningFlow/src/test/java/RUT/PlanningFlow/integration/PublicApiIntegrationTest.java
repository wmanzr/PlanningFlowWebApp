package RUT.PlanningFlow.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Интеграция: публичный API")
class PublicApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /api/v1/public/landing-stats без авторизации")
    void landingStats_permitAll() throws Exception {
        mockMvc.perform(get("/api/v1/public/landing-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedEventsCount").exists());
    }
}
