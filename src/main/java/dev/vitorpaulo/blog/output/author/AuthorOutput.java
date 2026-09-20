package dev.vitorpaulo.blog.output.author;

import com.clerk.backend_api.Clerk;
import com.clerk.backend_api.models.operations.ListOrganizationMembershipsRequest;
import dev.vitorpaulo.blog.common.exception.InternalException;
import dev.vitorpaulo.blog.common.util.PostUtils;
import dev.vitorpaulo.blog.config.security.AuthorRoleMapper;
import dev.vitorpaulo.blog.output.mapper.AuthorOutputMapper;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorOutput {

    private final AuthorRepository authorRepository;
    private final AuthorOutputMapper authorOutputMapper;
    private final Clerk clerk;

    @Value("${clerk.organization-id}")
    private String organizationId;

    public AuthorModel getAuthor(Jwt principal) {
        final var user = clerk.users()
            .get(principal.getSubject())
            .user()
            .orElseThrow(InternalException::new);

        final var author = authorRepository.findBySubjectId(principal.getSubject())
            .orElse(dev.vitorpaulo.blog.domain.AuthorEntity.builder()
                .subjectId(principal.getSubject())
                .build());

        final var name = (user.firstName().orElse("...") + " " + user.lastName().orElse("")).trim();
        author.setName(name);
        author.setAvatarUrl(user.imageUrl().orElse(null));

        if (author.getSlug() == null || author.getSlug().isBlank()) {
            author.setSlug(generateUniqueSlug(name, author.getId()));
        }

        authorRepository.save(author);

        final var role = resolveRole(principal, user.id());

        return authorOutputMapper.toModel(author, role);
    }

    private String resolveRole(Jwt principal, String clerkUserId) {
        var resolvedRole = resolveMembershipRole(clerkUserId);
        if (StringUtils.isNotBlank(resolvedRole)) {
            return resolvedRole;
        }
        return fallbackRole(principal);
    }

    private String resolveMembershipRole(String clerkUserId) {
        try {
            return clerk.organizationMemberships()
                .list(ListOrganizationMembershipsRequest.builder()
                    .userId(java.util.List.of(clerkUserId))
                    .organizationId(organizationId)
                    .build())
                .organizationMemberships()
                .orElseThrow(InternalException::new)
                .data()
                .stream()
                .findFirst()
                .map(membership -> membership.role())
                .orElse(null);
        } catch (Exception e) {
            log.warn("Clerk membership lookup failed for user {}", clerkUserId, e);
            return null;
        }
    }

    private String fallbackRole(Jwt principal) {
        if (principal.getClaim("org_role") instanceof String role && StringUtils.isNotBlank(role)) {
            return role;
        }
        return AuthorRoleMapper.MEMBER_CLERK_ROLE;
    }

    private String generateUniqueSlug(String name, java.util.UUID currentId) {
        final var base = PostUtils.slugify(name);
        final var counter = authorRepository.countBySlugAndIdNot(base, currentId);
        if (counter == 0) {
            return base;
        }
        return base + "-" + counter + 1;
    }
}
