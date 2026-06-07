package eu.lordbucket.cherryportal.core.identity.controller;

import eu.lordbucket.cherryportal.core.identity.repository.LocalCredentialRepository;
import eu.lordbucket.cherryportal.core.identity.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest boots the full application context (real beans, real security config).
// @AutoConfigureMockMvc wires in MockMvc so we can send requests without a real HTTP server.
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired LocalCredentialRepository localCredentialRepository;
    @Autowired
    AccountRepository accountRepository;

    // Clean state before each test so tests don't interfere with each other.
    // Credentials reference Users via FK, so credentials must be deleted first.
    @BeforeEach
    void setUp() {
        localCredentialRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void register_validData_returns200AndPersistsCredential() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","displayName":"Alice","password":"secret"}
                                """))
                .andExpect(status().isOk());

        assertTrue(localCredentialRepository.findByUsername("alice").isPresent());
    }

    @Test
    void register_duplicateUsername_returns400() throws Exception {
        String body = """
                {"username":"alice","displayName":"Alice","password":"secret"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        // Second registration with the same username should be rejected
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_returns200() throws Exception {
        register("alice", "Alice", "secret");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"secret"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        register("alice", "Alice", "secret");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"wrongpassword"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unknownUsername_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"nobody","password":"secret"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_invalidatesSession() throws Exception {
        register("alice", "Alice", "secret");

        // Login and capture the session that was created
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"secret"}
                                """))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/v1/auth/logout").session(session))
                .andExpect(status().isOk());

        assertTrue(session.isInvalid());
    }

    // Helper so tests don't repeat the register boilerplate
    private void register(String username, String displayName, String password) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format(
                        """
                        {"username":"%s","displayName":"%s","password":"%s"}
                        """, username, displayName, password)));
    }
}
