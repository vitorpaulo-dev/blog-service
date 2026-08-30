package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.common.util.PostUtils;
import dev.vitorpaulo.blog.config.exception.NotFoundException;
import dev.vitorpaulo.blog.config.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.config.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.input.response.PostResponse;
import dev.vitorpaulo.blog.mapper.PostMapper;
import dev.vitorpaulo.blog.model.post.Post;
import dev.vitorpaulo.blog.model.post.PostStatus;
import dev.vitorpaulo.blog.model.post.Project;
import dev.vitorpaulo.blog.model.post.Tag;
import dev.vitorpaulo.blog.output.post.PostOutput;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import dev.vitorpaulo.blog.output.tag.TagOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdatePostUseCase {

    private final PostOutput postOutput;
    private final TagOutput tagOutput;
    private final ProjectOutput projectOutput;
    private final PostMapper postMapper;

    @Transactional
    public PostResponse execute(UUID id, String title, String bannerUrl, String content, String language,
                               java.util.List<UUID> tagIds, java.util.List<UUID> projectIds, String statusStr) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ExceptionCode.UNAUTHORIZED, null);
        }
        Post existing = postOutput.findById(id)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.POST_NOT_FOUND));

        String clerkUserId = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = existing.authors().stream().anyMatch(a -> clerkUserId.equals(a.clerkUserId()));
        if (!isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, ExceptionCode.FORBIDDEN, Map.of("post", "Not owner"));
        }

        PostStatus currentStatus = existing.status() != null ? PostStatus.valueOf(existing.status()) : PostStatus.DRAFT;
        PostStatus status = parseStatus(statusStr, currentStatus);

        String newSlug = existing.slug();
        if (!existing.title().equals(title)) {
            newSlug = generateUniqueSlug(title, existing.id());
        }

        int[] reading = PostUtils.computeReadingTime(content);

        Set<Tag> tags = existing.tags();
        if (tagIds != null) {
            var fetched = new HashSet<>(tagOutput.findAllById(tagIds));
            if (fetched.size() != tagIds.size()) throw new NotFoundException(ExceptionCode.TAG_NOT_FOUND);
            tags = fetched;
        }
        Set<Project> projects = existing.projects();
        if (projectIds != null) {
            var fetched = new HashSet<>(projectOutput.findAllById(projectIds));
            if (fetched.size() != projectIds.size()) throw new NotFoundException(ExceptionCode.PROJECT_NOT_FOUND);
            projects = fetched;
        }

        Post updated = new Post(
                existing.id(),
                newSlug,
                title,
                bannerUrl,
                content,
                language,
                status.name(),
                existing.viewCount(),
                reading[0],
                reading[1],
                existing.lastViewedAt(),
                existing.createdAt(),
                existing.updatedAt(),
                existing.authors(),
                tags,
                projects,
                existing.reactionCount(),
                existing.commentCount()
        );

        Post saved = postOutput.save(updated);
        PostResponse r = postMapper.toResponse(saved);
        return new PostResponse(r.id(), r.slug(), r.title(), r.bannerUrl(), r.content(), r.language(), r.status(),
                r.viewCount(), r.averageReadingTimeSeconds(), r.estimatedReadingTimeMinutes(), r.lastViewedAt(), r.createdAt(), r.updatedAt(),
                r.authors(), r.tags(), r.projects(), 0L, 0L);
    }

    private PostStatus parseStatus(String statusStr, PostStatus current) {
        if (statusStr == null || statusStr.isBlank()) {
            return current != null ? current : PostStatus.DRAFT;
        }
        try {
            return PostStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ExceptionCode.FIELD_VALIDATION, Map.of("status", "Invalid status"));
        }
    }

    private String generateUniqueSlug(String title, UUID currentId) {
        String base = PostUtils.slugify(title);
        String slug = base;
        int counter = 1;
        while (true) {
            var existing = postOutput.findBySlug(slug);
            if (existing.isEmpty() || existing.get().id().equals(currentId)) break;
            slug = base + "-" + counter++;
        }
        return slug;
    }
}
