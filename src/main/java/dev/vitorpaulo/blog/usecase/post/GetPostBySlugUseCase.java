package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.config.exception.NotFoundException;
import dev.vitorpaulo.blog.config.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.input.response.PostResponse;
import dev.vitorpaulo.blog.mapper.PostMapper;
import dev.vitorpaulo.blog.model.ReactionTargetType;
import dev.vitorpaulo.blog.model.post.Post;
import dev.vitorpaulo.blog.model.post.PostStatus;
import dev.vitorpaulo.blog.output.PostOutput;
import dev.vitorpaulo.blog.output.ReactionOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class GetPostBySlugUseCase {

    private final PostOutput postOutput;
    private final ReactionOutput reactionOutput;
    private final PostMapper postMapper;

    @Transactional
    public PostResponse execute(String slug) {
        Post post = postOutput.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.POST_SLUG_NOT_FOUND));

        if (PostStatus.DRAFT.name().equals(post.status())) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser");
            if (!isAuthenticated) {
                throw new NotFoundException(ExceptionCode.POST_NOT_FOUND);
            }
            String clerkUserId = auth.getName();
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isOwner = post.authors().stream().anyMatch(a -> clerkUserId.equals(a.clerkUserId()));
            if (!isOwner && !isAdmin) {
                throw new NotFoundException(ExceptionCode.POST_NOT_FOUND);
            }
        }

        Post incremented = new Post(
                post.id(),
                post.slug(),
                post.title(),
                post.bannerUrl(),
                post.content(),
                post.language(),
                post.status(),
                post.viewCount() == null ? 1L : post.viewCount() + 1,
                post.averageReadingTimeSeconds(),
                post.estimatedReadingTimeMinutes(),
                OffsetDateTime.now(),
                post.createdAt(),
                post.updatedAt(),
                post.authors(),
                post.tags(),
                post.projects(),
                post.reactionCount(),
                post.commentCount()
        );
        Post saved = postOutput.save(incremented);

        long reactionCount = reactionOutput.countByTargetTypeAndTargetId(ReactionTargetType.POST, saved.id());
        PostResponse r = postMapper.toResponse(saved);
        return new PostResponse(r.id(), r.slug(), r.title(), r.bannerUrl(), r.content(), r.language(), r.status(),
                r.viewCount(), r.averageReadingTimeSeconds(), r.estimatedReadingTimeMinutes(), r.lastViewedAt(), r.createdAt(), r.updatedAt(),
                r.authors(), r.tags(), r.projects(), reactionCount, 0L);
    }
}
