package eu.lordbucket.cherryportal.core.auth.service;

import eu.lordbucket.cherryportal.core.auth.dto.RoleUpdatableProperties;
import eu.lordbucket.cherryportal.core.auth.model.Role;
import eu.lordbucket.cherryportal.core.auth.model.RoleAssignment;
import eu.lordbucket.cherryportal.core.auth.permission.PermissionRegistry;
import eu.lordbucket.cherryportal.core.auth.repository.RoleAssignmentRepository;
import eu.lordbucket.cherryportal.core.auth.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final PermissionRegistry permissionRegistry;

    public RoleService(RoleRepository roleRepository, RoleAssignmentRepository roleAssignmentRepository, PermissionRegistry permissionRegistry) {
        this.roleRepository = roleRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.permissionRegistry = permissionRegistry;
    }

    @Transactional
    public Role createRole(String name, String description, String emoji, String color, Set<String> permissions) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setEmoji(emoji);
        role.setColor(color);
        role.setPermissions(permissions != null ? permissions : new HashSet<>());
        return roleRepository.save(role);
    }

    @Transactional
    public void archiveRole(UUID roleId) {
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var assignments = roleAssignmentRepository.findAllByRoleId(roleId);
        assignments.forEach(a -> a.setRevokedAt(Instant.now()));
        roleAssignmentRepository.saveAll(assignments);

        role.setArchivedAt(Instant.now());
        roleRepository.save(role);
    }

    public List<Role> listRoles() {
        return roleRepository.findAllByArchivedAtIsNull();
    }

    public Role getRole(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Role updateRole(UUID roleId, RoleUpdatableProperties properties) {
        var role = getRole(roleId);
        if (properties.name() != null) role.setName(properties.name());
        if (properties.description() != null) role.setDescription(properties.description());
        if (properties.emoji() != null) role.setEmoji(properties.emoji());
        if (properties.color() != null) role.setColor(properties.color());
        return roleRepository.save(role);
    }

    @Transactional
    public void addPermission(UUID roleId, String key) {
        if (!permissionRegistry.isKnown(key))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown permission key: " + key);
        var role = getRole(roleId);
        role.getPermissions().add(key);
        roleRepository.save(role);
    }

    @Transactional
    public void removePermission(UUID roleId, String key) {
        if (!permissionRegistry.isKnown(key))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown permission key: " + key);
        var role = getRole(roleId);
        role.getPermissions().remove(key);
        roleRepository.save(role);
    }
}
