package eu.lordbucket.cherryportal.core.auth.permission;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CorePermissions implements PermissionProvider {

    public static final String ROLE_MANAGE = "core.role.manage";
    public static final String ACCOUNT_SUSPEND = "core.account.suspend";

    @Override
    public Set<String> permissions() {
        return Set.of(ROLE_MANAGE, ACCOUNT_SUSPEND);
    }
}
