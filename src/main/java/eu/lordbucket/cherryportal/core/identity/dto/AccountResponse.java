package eu.lordbucket.cherryportal.core.identity.dto;

import java.util.UUID;

public record AccountResponse(
        UUID id,
        String status
) {
}
