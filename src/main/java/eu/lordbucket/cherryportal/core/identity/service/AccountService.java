package eu.lordbucket.cherryportal.core.identity.service;

import eu.lordbucket.cherryportal.core.identity.model.Account;
import eu.lordbucket.cherryportal.core.identity.model.AccountStatus;
import eu.lordbucket.cherryportal.core.identity.model.Profile;
import eu.lordbucket.cherryportal.core.identity.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createAccount(String displayName) {
        Account account = new Account();
        account.setStatus(AccountStatus.ACTIVE);

        Profile profile = new Profile();
        profile.setDisplayName(displayName);
        profile.setAccount(account);
        account.setProfile(profile);

        return accountRepository.save(account);
    }
}
