package eu.lordbucket.cherryportal.core.audit;

import eu.lordbucket.cherryportal.core.audit.model.AuditOutcome;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class AuditEntry {
    private UUID actor;
    private UUID onBehalf;

    private String action;
    private String targetType;
    private UUID target;

    private UUID scopeUnit;

    @Enumerated(EnumType.STRING)
    private AuditOutcome outcome;
    private String summary;
    @Lob
    private String details;

    // TODO: Add hash chain
}

