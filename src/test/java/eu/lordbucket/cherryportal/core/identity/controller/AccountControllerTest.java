package eu.lordbucket.cherryportal.core.identity.controller;

import eu.lordbucket.cherryportal.core.identity.repository.LocalCredentialRepository;
import eu.lordbucket.cherryportal.core.identity.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired LocalCredentialRepository localCredentialRepository;
    @Autowired
    AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        localCredentialRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void me_withoutSession_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withValidSession_returnsAccountInfo() throws Exception {
        // Register a user first
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"alice","displayName":"Alice","password":"secret"}
                        """));

        // Login — Spring Security writes the SecurityContext into the session.
        // We capture the session object so we can pass it to the next request,
        // simulating what a browser does with the JSESSIONID cookie.
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/api/v1/accounts/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
