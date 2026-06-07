package eu.lordbucket.cherryportal.core.auth.permission;

import java.util.Set;

public interface PermissionProvider {
    Set<String> permissions();
}
