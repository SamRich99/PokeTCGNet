package it.unipi.poketcgnet.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImp implements UserDetailsService {

    private final AccountResolver accountResolver;

    public UserDetailsServiceImp(AccountResolver accountResolver) {
        this.accountResolver = accountResolver;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return accountResolver.findByUsername(username)
                .map(resolved -> UserPrincipal.build(resolved.account(), resolved.role()))
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con username: " + username));
    }
}
