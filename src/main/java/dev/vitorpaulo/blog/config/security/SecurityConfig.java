package dev.vitorpaulo.blog.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/reactions/**", "/v1/**/reactions/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/reactions/**", "/v1/**/reactions/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/v1/reactions/**", "/v1/**/reactions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/v1/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/v1/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/v1/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Default converter handles scope/scp -> SCOPE_* authorities
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // Primary org_role claim -> ROLE_* (Clerk organization role)
            Object orgRole = jwt.getClaim("org_role");
            if (orgRole instanceof String stringClaim) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + stringClaim.toUpperCase()));
            } else if (orgRole instanceof Collection<?> collection) {
                for (Object item : collection) {
                    if (item instanceof String s) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + s.toUpperCase()));
                    }
                }
            }

            // Merge scope authorities (SCOPE_*) without duplication
            Collection<GrantedAuthority> scopeAuthorities = scopeConverter.convert(jwt);
            if (scopeAuthorities != null) {
                for (GrantedAuthority ga : scopeAuthorities) {
                    if (authorities.stream().noneMatch(a -> a.getAuthority().equals(ga.getAuthority()))) {
                        authorities.add(ga);
                    }
                }
            }

            return authorities;
        });
        return converter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }
}
