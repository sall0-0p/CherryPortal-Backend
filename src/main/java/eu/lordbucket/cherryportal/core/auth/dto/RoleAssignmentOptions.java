package eu.lordbucket.cherryportal.core.auth.dto;

import java.time.Instant;

public record RoleAssignmentOptions(String reason, Instant startsAt, Instant expiresAt) {
    public RoleAssignmentOptions {
        if (startsAt == null) startsAt = Instant.now();
    }
}
