package eu.lordbucket.cherryportal.core.identity.repository;

import eu.lordbucket.cherryportal.core.identity.model.LocalCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocalCredentialRepository
        extends JpaRepository<LocalCredential, Long> {

    Optional<LocalCredential> findByUsername(String username);
}