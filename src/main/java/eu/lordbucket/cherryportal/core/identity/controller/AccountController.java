package eu.lordbucket.cherryportal.core.identity.controller;

import eu.lordbucket.cherryportal.core.identity.dto.AccountResponse;
import eu.lordbucket.cherryportal.core.identity.dto.ProfileResponse;
import eu.lordbucket.cherryportal.core.identity.model.LocalCredential;
import eu.lordbucket.cherryportal.core.identity.repository.AccountRepository;
import eu.lordbucket.cherryportal.core.identity.repository.LocalCredentialRepository;
import eu.lordbucket.cherryportal.core.identity.repository.ProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final LocalCredentialRepository localCredentialRepository;
    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;

    public AccountController(LocalCredentialRepository localCredentialRepository, AccountRepository accountRepository, ProfileRepository profileRepository) {
        this.localCredentialRepository = localCredentialRepository;
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
    }

    // @AuthenticationPrincipal injects the UserDetails object that
    // PortalUserDetailsService returned when this session was authenticated.
    // principal.getUsername() is the LocalCredential username, so we use
    // LocalCredentialRepository to get back to the User entity.
    @GetMapping("/me")
    public ResponseEntity<AccountResponse> me(@AuthenticationPrincipal UserDetails principal) {
        LocalCredential credential = localCredentialRepository.findByUsername(principal.getUsername())
                .orElseThrow();
        var account = credential.getAccount();
        return ResponseEntity.ok(new AccountResponse(account.getId(), account.getStatus().toString()));
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<?> getProfile(@PathVariable UUID id, @AuthenticationPrincipal UserDetails principal) {
        if (!accountRepository.existsById(id)) {
            return ResponseEntity.status(404).body("No such account exists.");
        }
        return profileRepository.findById(id)
                .map(p -> ResponseEntity.ok((Object) new ProfileResponse(p.getDisplayName())))
                .orElse(ResponseEntity.notFound().build());
    }
}
