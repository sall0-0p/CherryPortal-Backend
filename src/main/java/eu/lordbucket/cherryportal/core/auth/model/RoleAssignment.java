package eu.lordbucket.cherryportal.core.auth.model;

import eu.lordbucket.cherryportal.core.identity.model.Account;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "role_assignments", indexes = @Index(name = "ix_assignment_account", columnList = "account_id"))
public class RoleAssignment {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "role_id")
    private Role role;

    private String scope; // TODO: Replace currently global scope

    private Instant startsAt = Instant.now();
    private Instant expiresAt;
    private Instant revokedAt;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "granted_by")
    private Account grantedBy;
    private Instant grantedAt = Instant.now();
    private String reason;

    public boolean isEffective(Instant now) {
        return revokedAt == null
                && (startsAt == null || !startsAt.isAfter(now))
                && (expiresAt == null || now.isBefore(expiresAt));
    }
}
