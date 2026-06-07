package eu.lordbucket.cherryportal.core.auth.dto;

import java.time.Instant;
import java.util.UUID;

public record RoleAssignmentRequest(
        UUID roleId,
        String reason,
        Instant startsAt,
        Instant expiresAt
) {
}
