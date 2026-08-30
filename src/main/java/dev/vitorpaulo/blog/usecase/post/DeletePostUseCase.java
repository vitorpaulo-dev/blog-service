package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.config.exception.NotFoundException;
import dev.vitorpaulo.blog.config.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.config.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.model.post.Post;
import dev.vitorpaulo.blog.output.PostOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeletePostUseCase {

    private final PostOutput postOutput;

    @Transactional
    public void execute(UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ExceptionCode.UNAUTHORIZED, null);
        }
        Post post = postOutput.findById(id)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.POST_NOT_FOUND));
        String clerkUserId = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = post.authors().stream().anyMatch(a -> clerkUserId.equals(a.clerkUserId()));
        if (!isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, ExceptionCode.FORBIDDEN, Map.of("post", "Not owner"));
        }
        postOutput.delete(id);
    }
}
