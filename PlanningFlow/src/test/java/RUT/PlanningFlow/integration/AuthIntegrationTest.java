package RUT.PlanningFlow.integration;

import RUT.PlanningFlow.domain.enums.UserRoles;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Интеграция: аутентификация и регистрация")
class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Регистрация участника и вход по логину возвращают JWT")
    void registerParticipant_thenLogin_returnsTokens() throws Exception {
        final String username = uniqueUsername("part");
        final JsonNode reg = registerUser(username, username + "@it.test", UserRoles.PARTICIPANT);
        assertThat(reg.get("accessToken").asText()).isNotBlank();
        assertThat(reg.get("refreshToken").asText()).isNotBlank();

        final String access = login(username, KNOWN_PASSWORD);
        assertThat(access).isNotBlank();
    }

    @Test
    @DisplayName("Неверный пароль — 401 и код INVALID_CREDENTIALS")
    void login_wrongPassword_unauthorized() throws Exception {
        final String username = uniqueUsername("user");
        registerUser(username, username + "@it.test", UserRoles.PARTICIPANT);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"Wrong____1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("Регистрация с ролью ADMIN запрещена")
    void register_adminRole_notAllowed() throws Exception {
        final String username = uniqueUsername("badadmin");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s",
                                  "email": "%s",
                                  "fullName": "X",
                                  "birthDate": "1990-01-01",
                                  "role": "ADMIN"
                                }
                                """.formatted(username, KNOWN_PASSWORD, username + "@it.test")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("REGISTRATION_ROLE_NOT_ALLOWED"));
    }

    @Test
    @DisplayName("Обновление пары токенов по refresh")
    void refresh_returnsNewPair() throws Exception {
        final String username = uniqueUsername("refresh");
        final JsonNode reg = registerUser(username, username + "@it.test", UserRoles.ORGANIZER);
        final String refresh = reg.get("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }
}
