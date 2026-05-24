package RUT.PlanningFlow.integration;

import RUT.PlanningFlow.domain.enums.UserRoles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Интеграция: мероприятия и задачи (сквозной HTTP + JPA)")
class EventAndTaskIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Создание мероприятия → получение по id и пустой список задач")
    void createEvent_thenGetDetailsAndTasks() throws Exception {
        final String u = uniqueUsername("ev");
        final String token = registerUser(u, u + "@it.test", UserRoles.ORGANIZER).get("accessToken").asText();

        final String createBody = """
                {
                  "title": "Событие для интеграции",
                  "description": "Описание",
                  "startDate": "2026-08-10T09:00:00",
                  "endDate": "2026-08-10T21:00:00",
                  "latitude": 55.75,
                  "longitude": 37.62
                }
                """;
        final String rawId = mockMvc.perform(post("/api/v1/events")
                        .headers(bearerAuth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .trim();
        final int eventId = Integer.parseInt(rawId);

        mockMvc.perform(get("/api/v1/events/" + eventId).headers(bearerAuth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.title").value("Событие для интеграции"));

        mockMvc.perform(get("/api/v1/tasks/for-event/" + eventId).headers(bearerAuth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Список мероприятий организатора содержит созданное мероприятие")
    void listEvents_containsCreated() throws Exception {
        final String u = uniqueUsername("lst");
        final String token = registerUser(u, u + "@it.test", UserRoles.ORGANIZER).get("accessToken").asText();

        mockMvc.perform(post("/api/v1/events")
                        .headers(bearerAuth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Уникальное название списка",
                                  "startDate": "2026-09-01T08:00:00",
                                  "endDate": "2026-09-01T18:00:00"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/events")
                        .param("title", "Уникальное название списка")
                        .headers(bearerAuth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.items[0].title").value("Уникальное название списка"));
    }
}
