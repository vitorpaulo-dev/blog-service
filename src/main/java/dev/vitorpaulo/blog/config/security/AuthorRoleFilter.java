package dev.vitorpaulo.blog.config.security;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.usecase.author.FindOrCreateAuthorUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor
public class AuthorRoleFilter extends OncePerRequestFilter {

    public static final String AUTHOR_REQUEST_ATTRIBUTE = AuthorRoleFilter.class.getName() + ":author";

    private final FindOrCreateAuthorUseCase findOrCreateAuthorUseCase;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            try {
                publishResolvedAuthor(request, jwtAuthentication);
            } catch (Exception e) {
                log.warn("Failed to resolve Clerk author role for request", e);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void publishResolvedAuthor(HttpServletRequest request, JwtAuthenticationToken authentication) {
        final var author = findOrCreateAuthorUseCase.execute(authentication.getToken());
        request.setAttribute(AUTHOR_REQUEST_ATTRIBUTE, author);

        final var authorities = new ArrayList<GrantedAuthority>(authentication.getAuthorities());
        authorities.add(AuthorRoleMapper.toAuthority(author.role()));

        SecurityContextHolder.getContext().setAuthentication(
            new JwtAuthenticationToken(authentication.getToken(), authorities)
        );
    }
}
