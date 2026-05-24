package RUT.PlanningFlow.integration;

import RUT.PlanningFlow.domain.enums.UserRoles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Интеграция: правила Spring Security по HTTP")
class ApiAuthorizationIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /api/v1/events без токена — 401 или 403")
    void eventsList_withoutToken_rejects() throws Exception {
        final int status = mockMvc.perform(get("/api/v1/events"))
                .andReturn()
                .getResponse()
                .getStatus();
        assertThat(status).isIn(401, 403);
    }

    @Test
    @DisplayName("Участник не может создать мероприятие (POST /api/v1/events)")
    void createEvent_asParticipant_forbidden() throws Exception {
        final String u = uniqueUsername("p");
        final String token = registerUser(u, u + "@it.test", UserRoles.PARTICIPANT).get("accessToken").asText();

        final MockHttpServletRequestBuilder req = post("/api/v1/events")
                .headers(bearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validEventJson());
        mockMvc.perform(req).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Организатор может создать мероприятие")
    void createEvent_asOrganizer_created() throws Exception {
        final String u = uniqueUsername("org");
        final String token = registerUser(u, u + "@it.test", UserRoles.ORGANIZER).get("accessToken").asText();

        mockMvc.perform(post("/api/v1/events")
                        .headers(bearerAuth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEventJson()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Участник не может создать навык (POST /api/v1/skills)")
    void createSkill_asParticipant_forbidden() throws Exception {
        final String u = uniqueUsername("sp");
        final String token = registerUser(u, u + "@it.test", UserRoles.PARTICIPANT).get("accessToken").asText();

        mockMvc.perform(post("/api/v1/skills")
                        .headers(bearerAuth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Skill X\",\"category\":\"Cat\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Администратор может создать навык в каталоге")
    void createSkill_asAdmin_created() throws Exception {
        final String token = obtainAdminAccessToken();
        final String name = "it_skill_" + UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/v1/skills")
                        .headers(bearerAuth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"category\":\"Integration\"}"))
                .andExpect(status().isCreated());
    }

    private static String validEventJson() {
        return """
                {
                  "title": "Интеграционное мероприятие",
                  "description": "Тест",
                  "startDate": "2026-07-01T10:00:00",
                  "endDate": "2026-07-01T20:00:00"
                }
                """;
    }
}
