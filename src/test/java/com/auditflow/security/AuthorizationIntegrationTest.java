package com.auditflow.security;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationIntegrationTest {

    private static final String AUTH_COOKIE_NAME = "access_token";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("auditflow_test_db")
            .withUsername("auditflow_test_user")
            .withPassword("auditflow_test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        registry.add("app.security.cookie.secure", () -> "false");
        registry.add("app.security.cookie.same-site", () -> "Lax");
        registry.add("app.security.cookie.name", () -> AUTH_COOKIE_NAME);
        registry.add("app.security.cookie.max-age-seconds", () -> "86400");

        registry.add("app.security.jwt.secret", () -> "auditflow-test-secret-key-minimum-32-characters-123456");
        registry.add("app.security.jwt.expiration-ms", () -> "86400000");

        registry.add("app.security.cors.allowed-origins[0]", () -> "http://localhost:3000");
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Login correcto devuelve cookie HTTPOnly y no devuelve token en JSON")
    void loginSuccessReturnsHttpOnlyCookieAndNoTokenInBody() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "admin@auditflow.local",
                                "password", "Admin123!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(AUTH_COOKIE_NAME))
                .andExpect(jsonPath("$.email").value("admin@auditflow.local"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.jwt").doesNotExist())
                .andReturn();

        Cookie cookie = result.getResponse().getCookie(AUTH_COOKIE_NAME);

        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
    }

    @Test
    @DisplayName("Login incorrecto devuelve 401")
    void loginWithInvalidPasswordReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "admin@auditflow.local",
                                "password", "WrongPassword123!"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Endpoint protegido sin sesión devuelve 401")
    void protectedEndpointWithoutSessionReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Admin puede crear proyecto")
    void adminCanCreateProject() throws Exception {
        Cookie adminCookie = loginAsAdmin();

        mockMvc.perform(post("/api/projects")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectCreateJson("Admin Created Project")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Admin Created Project"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Auditor no puede crear proyecto")
    void auditorCannotCreateProject() throws Exception {
        Cookie auditorCookie = loginAsAuditor();

        mockMvc.perform(post("/api/projects")
                        .cookie(auditorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectCreateJson("Auditor Forbidden Project")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Auditor no puede editar proyecto")
    void auditorCannotEditProject() throws Exception {
        Cookie auditorCookie = loginAsAuditor();

        String updateBody = json(Map.of(
                "name", "Forbidden Update",
                "description", "Auditor should not update projects.",
                "target", "http://localhost:8080/api",
                "status", "IN_REVIEW"
        ));

        mockMvc.perform(put("/api/projects/1")
                        .cookie(auditorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Auditor no puede ver proyecto no asignado")
    void auditorCannotViewUnassignedProject() throws Exception {
        Cookie adminCookie = loginAsAdmin();
        Cookie auditorCookie = loginAsAuditor();

        Long unassignedProjectId = createProjectAsAdmin(adminCookie, "Unassigned Project");

        mockMvc.perform(get("/api/projects/" + unassignedProjectId)
                        .cookie(auditorCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Auditor puede crear finding en proyecto asignado")
    void auditorCanCreateFindingInAssignedProject() throws Exception {
        Cookie auditorCookie = loginAsAuditor();

        mockMvc.perform(post("/api/findings")
                        .cookie(auditorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(findingCreateJson(1L, "Auditor Assigned Finding")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.title").value("Auditor Assigned Finding"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("Auditor no puede crear finding en proyecto no asignado")
    void auditorCannotCreateFindingInUnassignedProject() throws Exception {
        Cookie adminCookie = loginAsAdmin();
        Cookie auditorCookie = loginAsAuditor();

        Long unassignedProjectId = createProjectAsAdmin(adminCookie, "Finding Forbidden Project");

        mockMvc.perform(post("/api/findings")
                        .cookie(auditorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(findingCreateJson(unassignedProjectId, "Forbidden Finding")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Filtros por severidad y estado funcionan")
    void findingFiltersWork() throws Exception {
        Cookie adminCookie = loginAsAdmin();

        mockMvc.perform(post("/api/findings")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(findingCreateJson(1L, "Filter Test Finding")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/findings")
                        .cookie(adminCookie)
                        .param("severity", "HIGH")
                        .param("status", "OPEN")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @DisplayName("Paginación funciona")
    void paginationWorks() throws Exception {
        Cookie adminCookie = loginAsAdmin();

        mockMvc.perform(get("/api/findings")
                        .cookie(adminCookie)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.first").exists())
                .andExpect(jsonPath("$.last").exists());
    }

    private Cookie loginAsAdmin() throws Exception {
        return login("admin@auditflow.local", "Admin123!");
    }

    private Cookie loginAsAuditor() throws Exception {
        return login("auditor@auditflow.local", "Auditor123!");
    }

    private Cookie login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(AUTH_COOKIE_NAME))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie(AUTH_COOKIE_NAME);

        assertThat(cookie).isNotNull();

        return cookie;
    }

    private Long createProjectAsAdmin(Cookie adminCookie, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectCreateJson(name)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

        return jsonNode.get("id").asLong();
    }

    private String projectCreateJson(String name) throws Exception {
        return json(Map.of(
                "name", name,
                "description", "Project created during automated authorization tests.",
                "target", "http://localhost:8080",
                "status", "ACTIVE"
        ));
    }

    private String findingCreateJson(Long projectId, String title) throws Exception {
        return json(Map.of(
                "projectId", projectId,
                "title", title,
                "description", "The application exposes a security issue detected during automated tests.",
                "recommendation", "Apply remediation controls and verify the fix.",
                "evidence", "Automated test evidence.",
                "severity", "HIGH",
                "status", "OPEN"
        ));
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}