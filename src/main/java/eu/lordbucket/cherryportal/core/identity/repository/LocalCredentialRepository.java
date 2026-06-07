package eu.lordbucket.cherryportal.core.identity.repository;

import eu.lordbucket.cherryportal.core.identity.model.LocalCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LocalCredentialRepository
        extends JpaRepository<LocalCredential, UUID> {

    Optional<LocalCredential> findByUsername(String username);
}