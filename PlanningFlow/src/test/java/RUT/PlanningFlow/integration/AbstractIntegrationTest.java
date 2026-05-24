package RUT.PlanningFlow.integration;

import RUT.PlanningFlow.adapter.out.persistence.entity.RoleEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.UserEntity;
import RUT.PlanningFlow.adapter.out.persistence.repository.RoleRepository;
import RUT.PlanningFlow.adapter.out.persistence.repository.UserRepository;
import RUT.PlanningFlow.domain.enums.UserRoles;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

 
@SpringBootTest
@AutoConfigureMockMvc
@Import(IntegrationTestAiStubConfiguration.class)
public abstract class AbstractIntegrationTest {

    protected static final String KNOWN_PASSWORD = "Testpass1";

    @Autowired
    protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void integrationBaseSetUp() {
        ensureCatalogRoles();
    }

    private void ensureCatalogRoles() {
        for (final UserRoles role : UserRoles.values()) {
            if (roleRepository.findByName(role).isEmpty()) {
                roleRepository.save(new RoleEntity(role));
            }
        }
    }

    protected String uniqueUsername(final String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    protected JsonNode registerUser(
            final String username,
            final String email,
            final UserRoles role
    ) throws Exception {
        final String body = """
                {
                  "username": "%s",
                  "password": "%s",
                  "email": "%s",
                  "fullName": "Integration User",
                  "birthDate": "1995-05-15",
                  "role": "%s"
                }
                """.formatted(username, KNOWN_PASSWORD, email, role.name());
        final MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

     
    protected String obtainAdminAccessToken() throws Exception {
        final String username = "it_admin";
        if (userRepository.findByUsername(username).isEmpty()) {
            final RoleEntity adminRole = roleRepository.findByName(UserRoles.ADMIN).orElseThrow();
            final UserEntity admin = new UserEntity(
                    username,
                    passwordEncoder.encode(KNOWN_PASSWORD),
                    "it_admin@integration.test",
                    "Integration Admin",
                    LocalDate.of(1990, 1, 1)
            );
            admin.setRoles(new ArrayList<>(List.of(adminRole)));
            userRepository.save(admin);
        }
        return login(username, KNOWN_PASSWORD);
    }

    protected String login(final String username, final String password) throws Exception {
        final String body = """
                {"username":"%s","password":"%s"}
                """.formatted(username, password);
        final MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    protected static HttpHeaders bearerAuth(final String accessToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }
}
