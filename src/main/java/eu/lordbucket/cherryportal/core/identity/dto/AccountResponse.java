package eu.lordbucket.cherryportal.core.identity.dto;

public record AccountResponse(
        Long id,
        String displayName,
        String status
) {
}
