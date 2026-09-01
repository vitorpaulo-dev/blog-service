package dev.vitorpaulo.blog.output.author;

import com.clerk.backend_api.Clerk;
import dev.vitorpaulo.blog.common.exception.InternalException;
import dev.vitorpaulo.blog.domain.AuthorEntity;
import dev.vitorpaulo.blog.output.mapper.AuthorMapper;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class AuthorOutput {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
	private final Clerk clerk;

    public AuthorModel getAuthor(Jwt principal) {
		final var user = clerk.users()
			.get(principal.getSubject())
			.user()
			.orElseThrow(InternalException::new);

		final var author = authorRepository.findBySubjectId(principal.getSubject())
			.orElse(AuthorEntity.builder().subjectId(principal.getSubject()).build());
		author.setName((user.firstName().orElse("...") + " " + user.lastName().orElse("")).trim());
		author.setAvatarUrl(user.imageUrl().orElse(null));
		authorRepository.save(author);

		final var organization = user.organizationMemberships()
			.orElse(Collections.emptyList())
			.stream()
			.findFirst()
			.orElseThrow(InternalException::new);
		return authorMapper.toModel(author, organization.role());
    }
}
