package com.nxtwave.tasktracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxtwave.tasktracker.common.enums.Priority;
import com.nxtwave.tasktracker.common.enums.Role;
import com.nxtwave.tasktracker.common.enums.TaskStatus;
import com.nxtwave.tasktracker.user.dto.UpdateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TasktrackerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void memberCanOnlyListAssignedTasksAndCannotCreateTasks() throws Exception {

        UserSession admin = registerAndLogin("Admin", "admin-rbac@example.com");
        UserSession member = registerAndLogin("Member", "member-rbac@example.com");
        UserSession otherMember = registerAndLogin("Other Member", "other-rbac@example.com");

        createTask(admin.accessToken(), member.id(), "Member Task", null);
        createTask(admin.accessToken(), otherMember.id(), "Other Member Task", null);

        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", bearer(member.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].assigneeId").value(member.id()));

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", bearer(member.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskBody(member.id(), "Not Allowed", null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void statusTransitionsAndRefreshRotationAreEnforced() throws Exception {

        UserSession admin = registerAndLogin("Admin Flow", "admin-flow@example.com");
        UserSession member = registerAndLogin("Member Flow", "member-flow@example.com");

        long taskId = createTask(admin.accessToken(), member.id(), "Workflow Task", null);

        mockMvc.perform(patch("/api/v1/tasks/{taskId}/status", taskId)
                        .header("Authorization", bearer(member.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", TaskStatus.IN_PROGRESS.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(TaskStatus.IN_PROGRESS.name()));

        mockMvc.perform(patch("/api/v1/tasks/{taskId}/status", taskId)
                        .header("Authorization", bearer(member.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", TaskStatus.DONE.name()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

        String oldRefreshToken = member.refreshToken();
        String refreshResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", oldRefreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode refreshed = objectMapper.readTree(refreshResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", oldRefreshToken))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", bearer(refreshed.get("accessToken").asText())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.id()));
    }

    @Test
    void managersCanManageProjectsButMembersCannot() throws Exception {

        UserSession admin = registerAndLogin("Admin Project", "admin-project@example.com");
        UserSession manager = registerAndLogin("Manager Project", "manager-project@example.com");
        UserSession member = registerAndLogin("Member Project", "member-project@example.com");

        promoteUser(admin.accessToken(), manager.id(), manager.name(), Role.MANAGER);

        long projectId = createProject(manager.accessToken(), "Launch Plan");
        long taskId = createTask(manager.accessToken(), member.id(), "Project Task", projectId);

        mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId)
                        .header("Authorization", bearer(member.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(projectId))
                .andExpect(jsonPath("$.projectName").value("Launch Plan"));

        mockMvc.perform(get("/api/v1/projects")
                        .header("Authorization", bearer(member.accessToken())))
                .andExpect(status().isForbidden());
    }

    private UserSession registerAndLogin(String name, String email) throws Exception {

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", name,
                                "email", email,
                                "password", "Password123"
                        ))))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "Password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode auth = objectMapper.readTree(loginResponse);
        String accessToken = auth.get("accessToken").asText();

        String profileResponse = mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode profile = objectMapper.readTree(profileResponse);

        return new UserSession(
                profile.get("id").asLong(),
                profile.get("name").asText(),
                accessToken,
                auth.get("refreshToken").asText()
        );
    }

    private long createTask(String accessToken, long assigneeId, String title, Long projectId) throws Exception {

        String response = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskBody(assigneeId, title, projectId))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private long createProject(String accessToken, String name) throws Exception {

        String response = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", name,
                                "description", "Project for integration test"
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private void promoteUser(String adminAccessToken, long userId, String name, Role role) throws Exception {

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName(name);
        request.setRole(role);

        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                        .header("Authorization", bearer(adminAccessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value(role.name()));
    }

    private Map<String, Object> taskBody(long assigneeId, String title, Long projectId) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", title);
        body.put("description", "Created from integration test");
        body.put("priority", Priority.MEDIUM.name());
        body.put("assigneeId", assigneeId);
        body.put("dueDate", LocalDate.now().plusDays(5).toString());

        if (projectId != null) {
            body.put("projectId", projectId);
        }

        return body;
    }

    private String bearer(String accessToken) {

        return "Bearer " + accessToken;
    }

    private record UserSession(
            long id,
            String name,
            String accessToken,
            String refreshToken
    ) {
    }
}
