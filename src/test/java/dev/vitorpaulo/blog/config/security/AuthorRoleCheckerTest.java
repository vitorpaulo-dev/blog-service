package dev.vitorpaulo.blog.config.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorRoleCheckerTest {

    private final AuthorRoleChecker authorRoleChecker = new AuthorRoleChecker();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isAuthenticated_nullAuthentication_returnsFalse() {
        SecurityContextHolder.clearContext();

        assertFalse(authorRoleChecker.isAuthenticated());
    }

    @Test
    void isAuthenticated_anonymousToken_returnsFalse() {
        SecurityContextHolder.getContext().setAuthentication(
            new AnonymousAuthenticationToken("key", "anonymous",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")))
        );

        assertFalse(authorRoleChecker.isAuthenticated());
    }

    @Test
    void isAuthenticated_authenticatedToken_returnsTrue() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("user", "credentials",
                List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        assertTrue(authorRoleChecker.isAuthenticated());
    }
}
