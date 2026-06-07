package eu.lordbucket.cherryportal.core.auth.dto;

import eu.lordbucket.cherryportal.core.auth.model.RoleAssignment;

import java.time.Instant;
import java.util.UUID;

public record RoleAssignmentResponse(
        UUID id,
        UUID roleId,
        String roleName,
        UUID grantedById,
        Instant grantedAt,
        Instant startsAt,
        Instant expiresAt,
        Instant revokedAt,
        String reason
) {
    public static RoleAssignmentResponse from(RoleAssignment a) {
        return new RoleAssignmentResponse(
                a.getId(),
                a.getRole().getId(),
                a.getRole().getName(),
                a.getGrantedBy() != null ? a.getGrantedBy().getId() : null,
                a.getGrantedAt(),
                a.getStartsAt(),
                a.getExpiresAt(),
                a.getRevokedAt(),
                a.getReason()
        );
    }
}
