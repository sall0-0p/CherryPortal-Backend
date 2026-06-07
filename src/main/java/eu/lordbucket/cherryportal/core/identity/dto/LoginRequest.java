package eu.lordbucket.cherryportal.core.identity.dto;

public record LoginRequest(
        String username,
        String password
) {
}
