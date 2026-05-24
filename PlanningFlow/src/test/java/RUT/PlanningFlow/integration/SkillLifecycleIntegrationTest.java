package RUT.PlanningFlow.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Интеграция: жизненный цикл навыка в каталоге (админ)")
class SkillLifecycleIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Создание навыка админом → удаление → GET возвращает 404")
    void createDeleteAndVerifyMissing() throws Exception {
        final String adminToken = obtainAdminAccessToken();
        final String name = "lifecycle_" + UUID.randomUUID().toString().substring(0, 8);

        final String rawId = mockMvc.perform(post("/api/v1/skills")
                        .headers(bearerAuth(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"category\":\"Lifecycle\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .trim();
        final int skillId = Integer.parseInt(rawId);

        mockMvc.perform(delete("/api/v1/skills/" + skillId).headers(bearerAuth(adminToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/skills/" + skillId).headers(bearerAuth(adminToken)))
                .andExpect(status().isNotFound());
    }
}
