package dev.vitorpaulo.blog.output.author;

import com.clerk.backend_api.Clerk;
import com.clerk.backend_api.models.operations.ListOrganizationMembershipsRequest;
import dev.vitorpaulo.blog.common.exception.InternalException;
import dev.vitorpaulo.blog.common.util.PostUtils;
import dev.vitorpaulo.blog.output.mapper.AuthorMapper;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorOutput {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
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

        final var organization = clerk.organizationMemberships()
            .list(ListOrganizationMembershipsRequest.builder()
                .userId(java.util.List.of(user.id()))
                .organizationId(organizationId)
                .build())
            .organizationMemberships()
            .orElseThrow(InternalException::new)
            .data()
            .stream()
            .findFirst()
            .orElseThrow(InternalException::new);

        return authorMapper.toModel(author, organization.roleName().orElseThrow(InternalException::new));
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
