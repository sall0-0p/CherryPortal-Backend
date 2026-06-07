package eu.lordbucket.cherryportal.core.identity.repository;

import eu.lordbucket.cherryportal.core.identity.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<Account, Long> {
}
