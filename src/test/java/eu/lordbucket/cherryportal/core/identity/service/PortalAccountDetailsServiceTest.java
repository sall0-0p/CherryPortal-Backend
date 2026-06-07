package eu.lordbucket.cherryportal.core.identity.service;

import eu.lordbucket.cherryportal.core.identity.model.Account;
import eu.lordbucket.cherryportal.core.identity.model.LocalCredential;
import eu.lordbucket.cherryportal.core.identity.repository.LocalCredentialRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// No @SpringBootTest — the service has no Spring dependencies, so we test it
// by constructing it directly. mock() creates a fake repository whose return
// values we control with when(...).thenReturn(...).
class PortalAccountDetailsServiceTest {

    private final LocalCredentialRepository repo = mock(LocalCredentialRepository.class);
    private final PortalUserDetailsService service = new PortalUserDetailsService(repo);

    @Test
    void loadUserByUsername_knownUser_returnsMatchingUserDetails() {
        LocalCredential credential = new LocalCredential();
        credential.setAccount(new Account());
        credential.setUsername("alice");
        credential.setPasswordHash("$2a$hashed");

        when(repo.findByUsername("alice")).thenReturn(Optional.of(credential));

        UserDetails result = service.loadUserByUsername("alice");

        assertEquals("alice", result.getUsername());
        assertEquals("$2a$hashed", result.getPassword());
    }

    @Test
    void loadUserByUsername_unknownUser_throwsUsernameNotFoundException() {
        when(repo.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("nobody"));
    }
}
