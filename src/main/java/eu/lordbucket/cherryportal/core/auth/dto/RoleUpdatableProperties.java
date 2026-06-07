package eu.lordbucket.cherryportal.core.auth.dto;

public record RoleUpdatableProperties (
        String name,
        String description,
        String emoji,
        String color
) {
    
}
