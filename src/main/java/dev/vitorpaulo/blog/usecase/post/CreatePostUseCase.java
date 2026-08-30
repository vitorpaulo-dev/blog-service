package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.common.util.PostUtils;
import dev.vitorpaulo.blog.config.exception.NotFoundException;
import dev.vitorpaulo.blog.config.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.config.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.input.response.PostResponse;
import dev.vitorpaulo.blog.mapper.PostMapper;
import dev.vitorpaulo.blog.model.post.Author;
import dev.vitorpaulo.blog.model.post.Post;
import dev.vitorpaulo.blog.model.post.PostStatus;
import dev.vitorpaulo.blog.model.post.Project;
import dev.vitorpaulo.blog.model.post.Tag;
import dev.vitorpaulo.blog.output.AuthorOutput;
import dev.vitorpaulo.blog.output.PostOutput;
import dev.vitorpaulo.blog.output.ProjectOutput;
import dev.vitorpaulo.blog.output.TagOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CreatePostUseCase {

    private final PostOutput postOutput;
    private final AuthorOutput authorOutput;
    private final TagOutput tagOutput;
    private final ProjectOutput projectOutput;
    private final PostMapper postMapper;

    @Transactional
    public PostResponse execute(String title, String bannerUrl, String content, String language,
                               java.util.List<java.util.UUID> tagIds, java.util.List<java.util.UUID> projectIds,
                               String statusStr) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ExceptionCode.UNAUTHORIZED, null);
        }
        String clerkUserId = auth.getName();
        Author author = resolveAuthor(clerkUserId, auth);

        PostStatus status = parseStatus(statusStr);

        String slug = generateUniqueSlug(title);

        int[] reading = PostUtils.computeReadingTime(content);

        Set<Tag> tags = tagIds == null ? new HashSet<>() : new HashSet<>(tagOutput.findAllById(tagIds));
        Set<Project> projects = projectIds == null ? new HashSet<>() : new HashSet<>(projectOutput.findAllById(projectIds));

        if (tagIds != null && !tagIds.isEmpty() && tags.size() != tagIds.size()) {
            throw new NotFoundException(ExceptionCode.TAG_NOT_FOUND);
        }
        if (projectIds != null && !projectIds.isEmpty() && projects.size() != projectIds.size()) {
            throw new NotFoundException(ExceptionCode.PROJECT_NOT_FOUND);
        }

        Post post = new Post(
                null,
                slug,
                title,
                bannerUrl,
                content,
                language,
                status.name(),
                0L,
                reading[0],
                reading[1],
                null,
                null,
                null,
                new HashSet<>(Set.of(author)),
                tags,
                projects,
                0L,
                0L
        );

        Post saved = postOutput.save(post);
        PostResponse response = postMapper.toResponse(saved);
        return new PostResponse(
                response.id(), response.slug(), response.title(), response.bannerUrl(), response.content(),
                response.language(), response.status(), response.viewCount(), response.averageReadingTimeSeconds(),
                response.estimatedReadingTimeMinutes(), response.lastViewedAt(), response.createdAt(), response.updatedAt(),
                response.authors(), response.tags(), response.projects(), 0L, 0L
        );
    }

    private Author resolveAuthor(String clerkUserId, Authentication auth) {
        return authorOutput.findByClerkUserId(clerkUserId).orElseGet(() -> {
            String name = clerkUserId;
            Object principal = auth.getPrincipal();
            if (principal instanceof Jwt jwt) {
                String claimName = jwt.getClaimAsString("name");
                if (claimName != null && !claimName.isBlank()) name = claimName;
                else {
                    String username = jwt.getClaimAsString("username");
                    if (username != null) name = username;
                }
            }
            Author newAuthor = new Author(null, clerkUserId, name, null, null);
            return authorOutput.save(newAuthor);
        });
    }

    private PostStatus parseStatus(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) return PostStatus.DRAFT;
        try {
            return PostStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ExceptionCode.FIELD_VALIDATION, java.util.Map.of("status", "Invalid status, must be DRAFT or PUBLISHED"));
        }
    }

    private String generateUniqueSlug(String title) {
        String base = PostUtils.slugify(title);
        String slug = base;
        int counter = 1;
        while (postOutput.existsBySlug(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }
}
