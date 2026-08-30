package dev.vitorpaulo.blog.output.author;

import dev.vitorpaulo.blog.mapper.PostMapper;
import dev.vitorpaulo.blog.model.post.Author;
import dev.vitorpaulo.blog.output.AuthorOutput;
import dev.vitorpaulo.blog.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthorOutputAdapter implements AuthorOutput {

    private final AuthorRepository authorRepository;
    private final PostMapper postMapper;

    @Override
    public Optional<Author> findByClerkUserId(String clerkUserId) {
        return authorRepository.findByClerkUserId(clerkUserId).map(postMapper::map);
    }

    @Override
    public Author save(Author author) {
        var entity = postMapper.toEntity(author);
        var saved = authorRepository.save(entity);
        return postMapper.map(saved);
    }
}
