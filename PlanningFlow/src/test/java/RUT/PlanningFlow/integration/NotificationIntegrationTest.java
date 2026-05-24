package RUT.PlanningFlow.integration;

import RUT.PlanningFlow.domain.enums.UserRoles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Интеграция: уведомления")
class NotificationIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Список уведомлений для авторизованного пользователя")
    void listNotifications_authenticated_ok() throws Exception {
        final String u = uniqueUsername("notif");
        final String token = registerUser(u, u + "@it.test", UserRoles.PARTICIPANT).get("accessToken").asText();

        mockMvc.perform(get("/api/v1/notifications").headers(bearerAuth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }
}
