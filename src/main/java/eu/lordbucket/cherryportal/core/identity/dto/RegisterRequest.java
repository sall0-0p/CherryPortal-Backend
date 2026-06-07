package eu.lordbucket.cherryportal.core.identity.dto;

public record RegisterRequest(
    String username,
    String displayName,
    String password
) {
}
