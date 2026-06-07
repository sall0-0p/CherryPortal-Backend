package eu.lordbucket.cherryportal.core.auth.repository;

import eu.lordbucket.cherryportal.core.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    List<Role> findAllByArchivedAtIsNull();
}
