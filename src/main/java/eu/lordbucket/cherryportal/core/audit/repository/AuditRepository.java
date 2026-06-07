package eu.lordbucket.cherryportal.core.audit.repository;

import eu.lordbucket.cherryportal.core.audit.model.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditRepository extends JpaRepository<AuditRecord, UUID> {
}
