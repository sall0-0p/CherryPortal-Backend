package eu.lordbucket.cherryportal.core.auth.controller;

import eu.lordbucket.cherryportal.core.auth.dto.RoleRequest;
import eu.lordbucket.cherryportal.core.auth.dto.RoleResponse;
import eu.lordbucket.cherryportal.core.auth.dto.RoleUpdatableProperties;
import eu.lordbucket.cherryportal.core.auth.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<RoleResponse> listRoles() {
        return roleService.listRoles().stream().map(RoleResponse::from).toList();
    }

    @PostMapping
    public RoleResponse createRole(@RequestBody RoleRequest req) {
        return RoleResponse.from(roleService.createRole(req.name(), req.description(), req.emoji(), req.color(), req.permissions()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRole(@PathVariable UUID id) {
        return ResponseEntity.ok(RoleResponse.from(roleService.getRole(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable UUID id, @RequestBody RoleUpdatableProperties properties) {
        return ResponseEntity.ok(RoleResponse.from(roleService.updateRole(id, properties)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveRole(@PathVariable UUID id) {
        roleService.archiveRole(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/permissions/{key}")
    public ResponseEntity<Void> addPermission(@PathVariable UUID id, @PathVariable String key) {
        roleService.addPermission(id, key);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permissions/{key}")
    public ResponseEntity<Void> removePermission(@PathVariable UUID id, @PathVariable String key) {
        roleService.removePermission(id, key);
        return ResponseEntity.noContent().build();
    }
}
