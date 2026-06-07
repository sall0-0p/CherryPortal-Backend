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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RoleControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired RoleAssignmentRepository roleAssignmentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired RoleRepository roleRepository;
    @Autowired LocalCredentialRepository localCredentialRepository;
    @Autowired AccountRepository accountRepository;

    MockHttpSession session;

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
    }

    @Test
    void listRoles_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listRoles_authenticated_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/roles").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createRole_returnsCreatedRoleWithAllFields() throws Exception {
        mockMvc.perform(post("/api/v1/roles").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Moderator","description":"Can moderate","emoji":"🛡","color":"#FF0000","permissions":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.name").value("Moderator"))
                .andExpect(jsonPath("$.description").value("Can moderate"))
                .andExpect(jsonPath("$.archivedAt").doesNotExist());
    }

    @Test
    void createRole_appearsInList() throws Exception {
        mockMvc.perform(post("/api/v1/roles").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Judge","description":"","emoji":"⚖","color":"#000","permissions":[]}
                        """));

        mockMvc.perform(get("/api/v1/roles").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Judge"));
    }

    @Test
    void getRole_existingId_returns200() throws Exception {
        String id = createRole("Admin");

        mockMvc.perform(get("/api/v1/roles/" + id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Admin"));
    }

    @Test
    void getRole_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/roles/00000000-0000-0000-0000-000000000000").session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRole_onlyChangesProvidedFields() throws Exception {
        String id = createRole("Original");

        mockMvc.perform(patch("/api/v1/roles/" + id).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Updated"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.description").value("Test description"));
    }

    @Test
    void archiveRole_returns204() throws Exception {
        String id = createRole("Temp");

        mockMvc.perform(delete("/api/v1/roles/" + id).session(session))
                .andExpect(status().isNoContent());
    }

    @Test
    void archiveRole_roleDisappearsFromList() throws Exception {
        String id = createRole("Temp");

        mockMvc.perform(delete("/api/v1/roles/" + id).session(session));

        mockMvc.perform(get("/api/v1/roles").session(session))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void addPermission_unknownKey_returns400() throws Exception {
        String id = createRole("TestRole");

        mockMvc.perform(post("/api/v1/roles/" + id + "/permissions/not.a.real.key").session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addPermission_validKey_returns204() throws Exception {
        String id = createRole("TestRole");

        mockMvc.perform(post("/api/v1/roles/" + id + "/permissions/core.role.manage").session(session))
                .andExpect(status().isNoContent());
    }

    @Test
    void removePermission_validKey_returns204() throws Exception {
        String id = createRole("TestRole");
        mockMvc.perform(post("/api/v1/roles/" + id + "/permissions/core.role.manage").session(session));

        mockMvc.perform(delete("/api/v1/roles/" + id + "/permissions/core.role.manage").session(session))
                .andExpect(status().isNoContent());
    }

    private String createRole(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/roles").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","description":"Test description","emoji":"","color":"","permissions":[]}
                                """.formatted(name)))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}
