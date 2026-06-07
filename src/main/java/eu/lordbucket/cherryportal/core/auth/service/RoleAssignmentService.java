package eu.lordbucket.cherryportal.core.auth.service;

import eu.lordbucket.cherryportal.core.auth.dto.RoleAssignmentOptions;
import eu.lordbucket.cherryportal.core.auth.model.RoleAssignment;
import eu.lordbucket.cherryportal.core.auth.repository.RoleAssignmentRepository;
import eu.lordbucket.cherryportal.core.auth.repository.RoleRepository;
import eu.lordbucket.cherryportal.core.identity.model.Account;
import eu.lordbucket.cherryportal.core.identity.repository.AccountRepository;
import eu.lordbucket.cherryportal.core.identity.repository.LocalCredentialRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RoleAssignmentService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final LocalCredentialRepository localCredentialRepository;

    public RoleAssignmentService(
            AccountRepository accountRepository,
            RoleRepository roleRepository,
            RoleAssignmentRepository roleAssignmentRepository,
            LocalCredentialRepository localCredentialRepository
    ) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.localCredentialRepository = localCredentialRepository;
    }

    @Transactional
    public RoleAssignment assignRole(UUID accountId, UUID roleId, String reason) {
        return assignRole(accountId, roleId, new RoleAssignmentOptions(reason, null, null));
    }

    @Transactional
    public RoleAssignment assignRole(UUID accountId, UUID roleId, RoleAssignmentOptions options) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (role.getArchivedAt() != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role is archived");

        var assignment = new RoleAssignment();
        assignment.setAccount(account);
        assignment.setRole(role);
        assignment.setReason(options.reason());
        assignment.setStartsAt(options.startsAt());
        assignment.setExpiresAt(options.expiresAt());
        assignment.setGrantedBy(resolveCurrentAccount());
        return roleAssignmentRepository.save(assignment);
    }

    @Transactional
    public RoleAssignment revokeRole(UUID assignmentId, String reason) {
        var assignment = roleAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (assignment.getRevokedAt() != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Assignment is already revoked");

        assignment.setRevokedAt(Instant.now());
        assignment.setReason(reason);
        return roleAssignmentRepository.save(assignment);
    }

    public List<RoleAssignment> getEffectiveAssignments(UUID accountId) {
        return roleAssignmentRepository.findAllEffectiveForAccount(accountId, Instant.now());
    }

    private Account resolveCurrentAccount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return localCredentialRepository.findByUsername(username)
                .map(c -> c.getAccount())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
