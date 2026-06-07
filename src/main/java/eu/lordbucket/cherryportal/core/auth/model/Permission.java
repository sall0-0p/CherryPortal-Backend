package eu.lordbucket.cherryportal.core.auth.model;

import lombok.Getter;

@Getter
public enum Permission {
    EXAMPLE_PERMISSION("core.example.permission");

    private String key;
    private Permission(String key) {
        this.key = key;
    }

    public static Permission fromKey(String key) {
        for (Permission p : values()) {
            if (p.key.equals(key)) return p;
        }
        throw new IllegalArgumentException("Unknown permission key: " + key);
    }
}
