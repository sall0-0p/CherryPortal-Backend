package eu.lordbucket.cherryportal.core.audit.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name="audit_records")
public class AuditRecord {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID record_id;

    @CreationTimestamp
    private Instant occured_at;

    private UUID actorAccountId;
    private UUID onBehalfOf;

    private String action;
    private String targetType;
    private UUID targetId;

    private UUID scopeUnitId;

    @Enumerated(EnumType.STRING)
    private AuditOutcome outcome;
    private String summary;
    @Lob
    private String details;

    // TODO: Add hash chain
}
