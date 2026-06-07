package eu.lordbucket.cherryportal.core.audit.service;

import eu.lordbucket.cherryportal.core.audit.AuditEntry;
import eu.lordbucket.cherryportal.core.audit.model.AuditRecord;
import eu.lordbucket.cherryportal.core.audit.repository.AuditRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void record(AuditEntry entry) {
        AuditRecord record = new AuditRecord();
        record.setAction(entry.getAction());
        record.setActorAccountId(entry.getActor());
        record.setTargetType(entry.getTargetType());
        record.setTargetId(entry.getTarget());
        record.setOutcome(entry.getOutcome());
        record.setSummary(entry.getSummary());
        record.setDetails(entry.getDetails());

        auditRepository.save(record);
    }
}
