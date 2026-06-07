package eu.lordbucket.cherryportal.core.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.lordbucket.cherryportal.core.auth.repository.RoleAssignmentRepository;
import eu.lordbucket.cherryportal.core.auth.repository.RoleRepository;
import eu.lordbucket.cherryportal.core.identity.repository.AccountRepository;
import eu.lordbucket.cherryportal.core.identity.repository.LocalCredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RoleAssignmentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired RoleAssignmentRepository roleAssignmentRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired LocalCredentialRepository localCredentialRepository;
    @Autowired AccountRepository accountRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    MockHttpSession session;
    UUID myAccountId;

    @BeforeEach
    void setUp() throws Exception {
        roleAssignmentRepository.deleteAll();
        roleRepository.deleteAll();
        localCredentialRepository.deleteAll();
        accountRepository.deleteAll();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"admin","displayName":"Admin","password":"secret"}
                        """));

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"secret"}
                                """))
                .andReturn();

        session = (MockHttpSession) login.getRequest().getSession(false);

        MvcResult me = mockMvc.perform(get("/api/v1/accounts/me").session(session)).andReturn();
        myAccountId = UUID.fromString(
                objectMapper.readTree(me.getResponse().getContentAsString()).get("id").asText()
        );
    }

    @Test
    void getEffectiveAssignments_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/" + myAccountId + "/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getEffectiveAssignments_noAssignments_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/" + myAccountId + "/roles").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void assignRole_validRequest_returnsAssignment() throws Exception {
        UUID roleId = createRole("Moderator");

        mockMvc.perform(post("/api/v1/accounts/" + myAccountId + "/roles").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roleId":"%s","reason":"Test assignment"}
                                """.formatted(roleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.roleId").value(roleId.toString()))
                .andExpect(jsonPath("$.roleName").value("Moderator"))
                .andExpect(jsonPath("$.reason").value("Test assignment"))
                .andExpect(jsonPath("$.revokedAt").doesNotExist())
                .andExpect(jsonPath("$.grantedById").isString());
    }

    @Test
    void assignRole_appearsInEffectiveList() throws Exception {
        UUID roleId = createRole("Moderator");

        mockMvc.perform(post("/api/v1/accounts/" + myAccountId + "/roles").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"roleId":"%s"}
                        """.formatted(roleId)));

        mockMvc.perform(get("/api/v1/accounts/" + myAccountId + "/roles").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].roleId").value(roleId.toString()));
    }

    @Test
    void assignRole_unknownAccount_returns404() throws Exception {
        UUID roleId = createRole("Moderator");

        mockMvc.perform(post("/api/v1/accounts/00000000-0000-0000-0000-000000000000/roles").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roleId":"%s"}
                                """.formatted(roleId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignRole_unknownRole_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/" + myAccountId + "/roles").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roleId":"00000000-0000-0000-0000-000000000000"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignRole_archivedRole_returns409() throws Exception {
        UUID roleId = createRole("Temp");

        mockMvc.perform(delete("/api/v1/roles/" + roleId).session(session));

        mockMvc.perform(post("/api/v1/accounts/" + myAccountId + "/roles").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roleId":"%s"}
                                """.formatted(roleId)))
                .andExpect(status().isConflict());
    }

    @Test
    void revokeRole_validAssignment_returns204() throws Exception {
        UUID roleId = createRole("Moderator");
        UUID assignmentId = assignRole(myAccountId, roleId);

        mockMvc.perform(delete("/api/v1/accounts/" + myAccountId + "/roles/" + assignmentId).session(session))
                .andExpect(status().isNoContent());
    }

    @Test
    void revokeRole_assignmentDisappearsFromEffectiveList() throws Exception {
        UUID roleId = createRole("Moderator");
        UUID assignmentId = assignRole(myAccountId, roleId);

        mockMvc.perform(delete("/api/v1/accounts/" + myAccountId + "/roles/" + assignmentId).session(session));

        mockMvc.perform(get("/api/v1/accounts/" + myAccountId + "/roles").session(session))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void revokeRole_alreadyRevoked_returns409() throws Exception {
        UUID roleId = createRole("Moderator");
        UUID assignmentId = assignRole(myAccountId, roleId);

        mockMvc.perform(delete("/api/v1/accounts/" + myAccountId + "/roles/" + assignmentId).session(session));

        mockMvc.perform(delete("/api/v1/accounts/" + myAccountId + "/roles/" + assignmentId).session(session))
                .andExpect(status().isConflict());
    }

    @Test
    void revokeRole_unknownAssignment_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/accounts/" + myAccountId + "/roles/00000000-0000-0000-0000-000000000000").session(session))
                .andExpect(status().isNotFound());
    }

    private UUID createRole(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/roles").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","description":"","emoji":"","color":"","permissions":[]}
                                """.formatted(name)))
                .andReturn();
        return UUID.fromString(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText()
        );
    }

    private UUID assignRole(UUID accountId, UUID roleId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/accounts/" + accountId + "/roles").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roleId":"%s"}
                                """.formatted(roleId)))
                .andReturn();
        return UUID.fromString(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText()
        );
    }
}
