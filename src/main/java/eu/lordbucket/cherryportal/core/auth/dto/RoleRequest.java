package eu.lordbucket.cherryportal.core.auth.dto;

import java.util.Set;

public record RoleRequest(
        String name,
        String description,
        String emoji,
        String color,
        Set<String> permissions
) {
}
