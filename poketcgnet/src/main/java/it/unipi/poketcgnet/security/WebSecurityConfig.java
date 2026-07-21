package it.unipi.poketcgnet.security;

import it.unipi.poketcgnet.security.AccountResolver;
import it.unipi.poketcgnet.security.jwt.AuthTokenFilter;
import it.unipi.poketcgnet.security.jwt.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final AccountResolver accountResolver;

    public WebSecurityConfig(UserDetailsService userDetailsService, JwtUtils jwtUtils,
            AccountResolver accountResolver) {
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
        this.accountResolver = accountResolver;
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter(jwtUtils, accountResolver);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"error\": \"Accesso Negato: Devi effettuare il login o inviare un token valido\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"error\": \"Accesso Negato: Permessi insufficienti (Ruolo errato per questa operazione)\"}");
                        }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Forward interno di Spring per la gestione errori: senza questo
                        // ogni 400/404/500 non gestito viene riscritto come 401
                        .requestMatchers("/error").permitAll()

                        // Rotte pubbliche per login e registrazione
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/trainers/register").permitAll()
                        .requestMatchers("/api/gyms/register").permitAll()

                        // Documentazione API (Swagger UI / OpenAPI)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Lettura pubblica: un utente non registrato può fare le stesse
                        // operazioni di LETTURA che farebbe un trainer loggato
                        .requestMatchers(HttpMethod.GET, "/api/cards/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/decks/**").permitAll()

                        // Homepage trainer: tornei in registration dei gym seguiti
                        .requestMatchers(HttpMethod.GET, "/api/tournaments/following").hasRole("TRAINER")
                        // Analytics admin (tornei conclusi per mese)
                        .requestMatchers(HttpMethod.GET, "/api/tournaments/analytics/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/tournaments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/archetypes/**").permitAll()
                        // Gym per prefettura ordinate per voto
                        .requestMatchers(HttpMethod.GET, "/api/gyms/prefecture/*/ranked").permitAll()

                        // Regole di autorizzazione per ruolo sulle operazioni di scrittura:
                        .requestMatchers(HttpMethod.POST, "/api/decks/**").hasRole("TRAINER") // i Trainer creano mazzi
                        .requestMatchers(HttpMethod.PUT, "/api/decks/**").hasRole("TRAINER")
                        // DELETE deck: trainer = soft delete del proprio, admin = hard delete
                        .requestMatchers(HttpMethod.DELETE, "/api/decks/**").hasAnyRole("TRAINER", "ADMIN")

                        // Iscrizione/disiscrizione ai tornei
                        .requestMatchers(HttpMethod.DELETE, "/api/tournaments/*/participants/*").hasRole("GYM")
                        .requestMatchers(HttpMethod.POST, "/api/tournaments/*/participants/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/tournaments/*/participants/**").hasRole("TRAINER")

                        .requestMatchers(HttpMethod.POST, "/api/tournaments/**").hasRole("GYM") // Le Gym creano tornei
                        .requestMatchers(HttpMethod.PUT, "/api/tournaments/**").hasRole("GYM") // modifica + classifica
                        .requestMatchers(HttpMethod.DELETE, "/api/tournaments/**").hasAnyRole("GYM", "ADMIN")

                        // /match resta usabile da chiunque sia loggato
                        .requestMatchers(HttpMethod.POST, "/api/archetypes/match").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/cards/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/cards/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/cards/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/archetypes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/archetypes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/archetypes/**").hasRole("ADMIN")

                        // Follow/unfollow sul grafo: solo i Trainer seguono (mai un Gym)
                        .requestMatchers(HttpMethod.POST, "/api/social/following/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/social/following/**").hasRole("TRAINER")
                        // Trainer fedeli
                        .requestMatchers(HttpMethod.GET, "/api/social/loyal-trainers").hasRole("GYM")

                        // Eliminazione account: self-service Trainer/Gym su /me, moderazione
                        // ADMIN su un account altrui
                        .requestMatchers(HttpMethod.DELETE, "/api/gyms/*/reviews/**").hasAnyRole("TRAINER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/trainers/me").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/gyms/me").hasRole("GYM")
                        .requestMatchers(HttpMethod.DELETE, "/api/trainers/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/gyms/*").hasRole("ADMIN")

                        // Tutti gli altri endpoint non specificati sopra (es. cercare dettagli utente)
                        // richiedono l'autenticazione
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider());

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
