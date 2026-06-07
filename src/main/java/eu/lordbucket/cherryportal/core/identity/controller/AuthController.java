package eu.lordbucket.cherryportal.core.identity.controller;

import eu.lordbucket.cherryportal.core.identity.dto.LoginRequest;
import eu.lordbucket.cherryportal.core.identity.dto.RegisterRequest;
import eu.lordbucket.cherryportal.core.identity.model.LocalCredential;
import eu.lordbucket.cherryportal.core.identity.repository.LocalCredentialRepository;
import eu.lordbucket.cherryportal.core.identity.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LocalCredentialRepository localCredentialRepository;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            LocalCredentialRepository localCredentialRepository,
            AccountService accountService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository
    ) {
        this.localCredentialRepository = localCredentialRepository;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (localCredentialRepository.findByUsername(req.username()).isPresent())
            return ResponseEntity.badRequest().body("Username taken");

        var account = accountService.createAccount(req.displayName());
        LocalCredential credential = new LocalCredential();
        credential.setAccount(account);
        credential.setUsername(req.username());
        credential.setPasswordHash(passwordEncoder.encode(req.password()));
        localCredentialRepository.save(credential);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest req,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // authenticate() throws AuthenticationException (e.g. BadCredentialsException)
        // if credentials are wrong. Spring MVC has no handler for that, so we catch
        // it here and return 401 ourselves instead of letting it become a 500.
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Create a fresh SecurityContext (never reuse the current one — avoids
        // session fixation attacks), put the verified Authentication into it,
        // then save it to the session so every subsequent request finds it.
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // false = don't create a session if there isn't one already
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}
