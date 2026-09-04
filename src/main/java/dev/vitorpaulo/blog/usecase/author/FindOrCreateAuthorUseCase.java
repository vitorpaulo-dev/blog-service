package dev.vitorpaulo.blog.usecase.author;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.output.author.AuthorOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FindOrCreateAuthorUseCase {

    private final AuthorOutput authorOutput;

    public AuthorModel execute(Jwt principal) {
       	return authorOutput.getAuthor(principal);
    }
}
