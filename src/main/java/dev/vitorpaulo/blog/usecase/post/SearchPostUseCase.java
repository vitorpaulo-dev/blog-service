package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.config.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.config.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.input.response.PostResponse;
import dev.vitorpaulo.blog.mapper.PostMapper;
import dev.vitorpaulo.blog.model.ReactionTargetType;
import dev.vitorpaulo.blog.model.post.Post;
import dev.vitorpaulo.blog.model.post.PostStatus;
import dev.vitorpaulo.blog.output.post.PostOutput;
import dev.vitorpaulo.blog.output.reaction.ReactionOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchPostUseCase {

    private final PostOutput postOutput;
    private final ReactionOutput reactionOutput;
    private final PostMapper postMapper;

    private static final Set<String> ALLOWED_SORTS = Set.of("createdAt", "updatedAt", "title", "slug", "viewCount", "created_at", "view_count");

    public GenericPageableResponse<PostResponse> execute(String query, Integer page, Integer limit, String sort) {
        int p = page == null ? 0 : page;
        int l = limit == null ? 5 : limit;
        String s = sort == null || sort.isBlank() ? "createdAt" : sort;

        if (p < 0) throw new BusinessException(HttpStatus.BAD_REQUEST, ExceptionCode.INVALID_PAGINATION, java.util.Map.of("page", "page must be >=0"));
        if (l < 1 || l > 50) throw new BusinessException(HttpStatus.BAD_REQUEST, ExceptionCode.INVALID_PAGINATION, java.util.Map.of("limit", "limit must be between 1 and 50"));
        if (!ALLOWED_SORTS.contains(s)) throw new BusinessException(HttpStatus.BAD_REQUEST, ExceptionCode.INVALID_SORT, java.util.Map.of("sort", "Invalid sort field. Allowed: " + ALLOWED_SORTS));

        String sortProp = mapSortProperty(s);
        Pageable pageable = PageRequest.of(p, l, Sort.by(Sort.Direction.DESC, sortProp));

        String q = (query == null || query.isBlank()) ? null : query.trim();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser");
        boolean isAdmin = isAuthenticated && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Page<Post> pageResult;
        if (isAdmin) {
            if (q == null) {
                pageResult = postOutput.findAll(pageable);
            } else {
                pageResult = postOutput.search(q, null, pageable);
            }
        } else if (isAuthenticated) {
            String clerkUserId = auth.getName();
            pageResult = postOutput.searchVisible(q, clerkUserId, pageable);
        } else {
            if (q == null) {
                pageResult = postOutput.findByStatus(PostStatus.PUBLISHED, pageable);
            } else {
                pageResult = postOutput.searchPublished(q, pageable);
            }
        }

        List<PostResponse> mapped = pageResult.getContent().stream()
                .map(post -> {
                    PostResponse r = postMapper.toResponse(post);
                    long rc = reactionOutput.countByTargetTypeAndTargetId(ReactionTargetType.POST, post.id());
                    return new PostResponse(r.id(), r.slug(), r.title(), r.bannerUrl(), r.content(), r.language(), r.status(),
                            r.viewCount(), r.averageReadingTimeSeconds(), r.estimatedReadingTimeMinutes(), r.lastViewedAt(), r.createdAt(), r.updatedAt(),
                            r.authors(), r.tags(), r.projects(), rc, 0L);
                })
                .toList();

        return new GenericPageableResponse<>(mapped, pageResult.getTotalPages(), pageResult.getTotalElements());
    }

    private String mapSortProperty(String sort) {
        return switch (sort) {
            case "viewCount", "view_count" -> "viewCount";
            case "created_at" -> "createdAt";
            case "updatedAt" -> "updatedAt";
            case "title" -> "title";
            case "slug" -> "slug";
            default -> "createdAt";
        };
    }
}
