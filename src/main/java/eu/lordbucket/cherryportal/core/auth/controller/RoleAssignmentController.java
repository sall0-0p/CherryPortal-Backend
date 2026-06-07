package eu.lordbucket.cherryportal.core.auth.controller;

import eu.lordbucket.cherryportal.core.auth.dto.RoleAssignmentOptions;
import eu.lordbucket.cherryportal.core.auth.dto.RoleAssignmentRequest;
import eu.lordbucket.cherryportal.core.auth.dto.RoleAssignmentResponse;
import eu.lordbucket.cherryportal.core.auth.service.RoleAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/roles")
public class RoleAssignmentController {

    private final RoleAssignmentService roleAssignmentService;

    public RoleAssignmentController(RoleAssignmentService roleAssignmentService) {
        this.roleAssignmentService = roleAssignmentService;
    }

    @GetMapping
    public List<RoleAssignmentResponse> getEffectiveAssignments(@PathVariable UUID accountId) {
        return roleAssignmentService.getEffectiveAssignments(accountId)
                .stream().map(RoleAssignmentResponse::from).toList();
    }

    @PostMapping
    public RoleAssignmentResponse assignRole(@PathVariable UUID accountId, @RequestBody RoleAssignmentRequest req) {
        var options = new RoleAssignmentOptions(req.reason(), req.startsAt(), req.expiresAt());
        return RoleAssignmentResponse.from(roleAssignmentService.assignRole(accountId, req.roleId(), options));
    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> revokeRole(@PathVariable UUID accountId, @PathVariable UUID assignmentId, @RequestParam(required = false) String reason) {
        roleAssignmentService.revokeRole(assignmentId, reason);
        return ResponseEntity.noContent().build();
    }
}
