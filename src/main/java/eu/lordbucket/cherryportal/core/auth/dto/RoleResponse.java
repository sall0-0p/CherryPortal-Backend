package eu.lordbucket.cherryportal.core.auth.dto;

import eu.lordbucket.cherryportal.core.auth.model.Role;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name,
        String description,
        String emoji,
        String color,
        Set<String> permissions,
        Instant createdAt,
        Instant archivedAt
) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getEmoji(),
                role.getColor(),
                role.getPermissions(),
                role.getCreatedAt(),
                role.getArchivedAt()
        );
    }
}
