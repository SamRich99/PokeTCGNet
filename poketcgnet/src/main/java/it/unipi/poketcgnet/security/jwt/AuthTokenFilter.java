package it.unipi.poketcgnet.security.jwt;

import it.unipi.poketcgnet.security.AccountResolver;
import it.unipi.poketcgnet.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final AccountResolver accountResolver;

    public AuthTokenFilter(JwtUtils jwtUtils, AccountResolver accountResolver) {
        this.jwtUtils = jwtUtils;
        this.accountResolver = accountResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String headerAuth = request.getHeader("Authorization");

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            String jwt = headerAuth.substring(7);

            if (jwtUtils.validateJwtToken(jwt)) {

                String username = jwtUtils.getUsernameFromJwtToken(jwt);
                String uid = jwtUtils.getUidFromJwtToken(jwt);
                List<String> roles = jwtUtils.getRolesFromJwtToken(jwt);

                // Il token è stateless ma l'identità è l'_id interno, non lo username:
                // verifico che l'utente con quello username esista ANCORA e sia LO STESSO
                // account per cui il token è stato emesso (stesso _id)
                Optional<AccountResolver.Resolved> resolved = accountResolver.findByUsername(username);
                if (resolved.isEmpty() || (uid != null && !uid.equals(resolved.get().account().getId()))) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"error\": \"Accesso Negato: Token non più valido (account eliminato o username riassegnato)\"}");
                    return;
                }

                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                UserPrincipal userPrincipal = new UserPrincipal(resolved.get().account().getId(), username, null,
                        authorities);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // Token presente ma non valido (scaduto o firma errata)
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Accesso Negato: Token non valido o scaduto\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}