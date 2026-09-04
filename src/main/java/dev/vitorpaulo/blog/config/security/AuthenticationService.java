package dev.vitorpaulo.blog.config.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    public String getCurrentUserId(Jwt principal) {
        return principal.getSubject();
    }

    public boolean isAdmin(Jwt principal) {
        final Object orgRole = principal.getClaim("org_role");
        if (orgRole instanceof String s) {
            return "ADMIN".equalsIgnoreCase(s);
        }
        if (orgRole instanceof java.util.Collection<?> roles) {
            return roles.stream()
                    .filter(r -> r instanceof String)
                    .map(r -> (String) r)
                    .anyMatch(r -> "ADMIN".equalsIgnoreCase(r));
        }
        return false;
    }
}
