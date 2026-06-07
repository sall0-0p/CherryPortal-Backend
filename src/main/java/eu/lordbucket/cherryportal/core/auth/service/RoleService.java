package eu.lordbucket.cherryportal.core.auth.service;

import eu.lordbucket.cherryportal.core.auth.dto.RoleUpdatableProperties;
import eu.lordbucket.cherryportal.core.auth.model.Permission;
import eu.lordbucket.cherryportal.core.auth.model.Role;
import eu.lordbucket.cherryportal.core.auth.repository.RoleAssignmentRepository;
import eu.lordbucket.cherryportal.core.auth.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;

    public RoleService(RoleRepository roleRepository, RoleAssignmentRepository roleAssignmentRepository) {
        this.roleRepository = roleRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
    }

    @Transactional
    public Role createRole(
            String name,
            String description,
            String emoji,
            String color,
            Set<String> permissions
    ) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setEmoji(emoji);
        role.setColor(color);
        role.setPermissions(permissions);

        return roleRepository.save(role);
    }

    @Transactional
    public void archiveRole(UUID roleId) {
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var assignments = roleAssignmentRepository.findAllByRoleId(roleId);
        assignments.forEach(roleAssignment -> {
            roleAssignment.setRevokedAt(Instant.now());
        });
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
    public void addPermission(UUID roleId, Permission permission) {
        var role = getRole(roleId);
        var permissions = role.getPermissions();
        permissions.add(permission.getKey());
        roleRepository.save(role);
    }

    @Transactional
    public void removePermission(UUID roleId, Permission permission) {
        var role = getRole(roleId);
        var permissions = role.getPermissions();
        permissions.remove(permission.getKey());
        roleRepository.save(role);
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
}

