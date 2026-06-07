package eu.lordbucket.cherryportal.core.identity.service;

import eu.lordbucket.cherryportal.core.identity.model.LocalCredential;
import eu.lordbucket.cherryportal.core.identity.repository.LocalCredentialRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortalUserDetailsService implements UserDetailsService {
    private LocalCredentialRepository creds;

    public PortalUserDetailsService(LocalCredentialRepository creds) {
        this.creds = creds;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        LocalCredential c = creds.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return User
                .withUsername(c.getUsername())
                .password(c.getPasswordHash())
                .authorities(List.of())
                .build();

    }
}
