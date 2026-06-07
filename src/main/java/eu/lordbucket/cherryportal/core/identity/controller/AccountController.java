package eu.lordbucket.cherryportal.core.identity.controller;

import eu.lordbucket.cherryportal.core.identity.dto.AccountResponse;
import eu.lordbucket.cherryportal.core.identity.model.Account;
import eu.lordbucket.cherryportal.core.identity.model.LocalCredential;
import eu.lordbucket.cherryportal.core.identity.repository.LocalCredentialRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final LocalCredentialRepository localCredentialRepository;

    public AccountController(LocalCredentialRepository localCredentialRepository) {
        this.localCredentialRepository = localCredentialRepository;
    }

    // @AuthenticationPrincipal injects the UserDetails object that
    // PortalUserDetailsService returned when this session was authenticated.
    // principal.getUsername() is the LocalCredential username, so we use
    // LocalCredentialRepository to get back to the User entity.
    @GetMapping("/me")
    public ResponseEntity<AccountResponse> me(@AuthenticationPrincipal UserDetails principal) {
        LocalCredential credential = localCredentialRepository.findByUsername(principal.getUsername())
                .orElseThrow();
        Account account = credential.getAccount();
        return ResponseEntity.ok(new AccountResponse(account.getId(), account.getDisplayName(), account.getStatus().toString()));
    }
}
