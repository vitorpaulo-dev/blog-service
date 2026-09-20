package dev.vitorpaulo.blog.output.author;

import com.clerk.backend_api.Clerk;
import com.clerk.backend_api.models.operations.ListOrganizationMembershipsRequest;
import dev.vitorpaulo.blog.common.exception.InternalException;
import dev.vitorpaulo.blog.common.util.PostUtils;
import dev.vitorpaulo.blog.config.security.AuthorRoleMapper;
import dev.vitorpaulo.blog.domain.AuthorEntity;
import dev.vitorpaulo.blog.output.mapper.AuthorOutputMapper;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

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
            .orElse(AuthorEntity.builder()
                .subjectId(principal.getSubject())
                .build());

        final var name = (user.firstName().orElse("...") + " " + user.lastName().orElse("")).trim();
        author.setName(name);
        author.setAvatarUrl(user.imageUrl().orElse(null));

        if (author.getSlug() == null || author.getSlug().isBlank()) {
            author.setSlug(generateUniqueSlug(name, author.getId()));
        }

        authorRepository.save(author);

        final var role = resolveRole(user.id());

        return authorOutputMapper.toModel(author, role);
    }

    private String resolveRole(String clerkUserId) {
        var resolvedRole = resolveMembershipRole(clerkUserId);
        if (resolvedRole == null) {
            return AuthorRoleMapper.MEMBER_CLERK_ROLE;
        }
        return resolvedRole;
    }

    private String resolveMembershipRole(String clerkUserId) {
        try {
            return clerk.organizationMemberships()
                .list(ListOrganizationMembershipsRequest.builder()
                    .userId(List.of(clerkUserId))
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

    private String generateUniqueSlug(String name, UUID currentId) {
        final var base = PostUtils.slugify(name);
        final var counter = authorRepository.countBySlugAndIdNot(base, currentId);
        if (counter == 0) {
            return base;
        }
        return base + "-" + counter + 1;
    }
}
