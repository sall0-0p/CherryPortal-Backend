package eu.lordbucket.cherryportal.core.auth.permission;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionRegistry {

    private final Set<String> knownKeys;

    public PermissionRegistry(List<PermissionProvider> providers) {
        this.knownKeys = providers.stream()
                .flatMap(p -> p.permissions().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isKnown(String key) {
        return knownKeys.contains(key);
    }

    public Set<String> allKnown() {
        return knownKeys;
    }
}
