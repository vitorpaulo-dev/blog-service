package dev.vitorpaulo.blog.config.security;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.usecase.author.FindOrCreateAuthorUseCase;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorRoleFilterTest {

    @Mock
    private FindOrCreateAuthorUseCase findOrCreateAuthorUseCase;

    private final Jwt jwt = new Jwt("raw", Instant.now(), Instant.now().plusSeconds(3600),
        Map.of("alg", "RS256"), Map.of("sub", "user_sub"));

    private AuthorRoleFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AuthorRoleFilter(findOrCreateAuthorUseCase);
    }

    @org.junit.jupiter.api.AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void successfulResolution_addsAuthorRoleAuthoritiesAndCachesAuthor() throws Exception {
        var authentication = new JwtAuthenticationToken(jwt, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        var author = new AuthorModel(null, "Author", null, null, null, "org:admin");
        when(findOrCreateAuthorUseCase.execute(any(Jwt.class))).thenReturn(author);

        var request = new MockHttpServletRequest();
        var chain = mockChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertTrue(chain.executed);
        assertEquals(author, request.getAttribute(AuthorRoleFilter.AUTHOR_REQUEST_ATTRIBUTE));
        var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals(AuthorRoleMapper.ADMIN_AUTHORITY, authorities.iterator().next().getAuthority());
    }

    @Test
    void resolutionFailure_continuesChainWithJwtAuthorities() throws Exception {
        var authentication = new JwtAuthenticationToken(jwt, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(findOrCreateAuthorUseCase.execute(any(Jwt.class))).thenThrow(new RuntimeException("clerk down"));

        var request = new MockHttpServletRequest();
        var chain = mockChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertTrue(chain.executed);
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty());
    }

    private ChainSpy mockChain() {
        return new ChainSpy();
    }

    private static final class ChainSpy implements jakarta.servlet.FilterChain {
        private boolean executed;

        @Override
        public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response)
            throws java.io.IOException, jakarta.servlet.ServletException {
            executed = true;
        }
    }
}
