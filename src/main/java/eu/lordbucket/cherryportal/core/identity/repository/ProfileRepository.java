package eu.lordbucket.cherryportal.core.identity.repository;

import eu.lordbucket.cherryportal.core.identity.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
