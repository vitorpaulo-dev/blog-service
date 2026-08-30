package dev.vitorpaulo.blog.output.author;

import dev.vitorpaulo.blog.model.post.Author;

import java.util.Optional;

public interface AuthorOutput {

    Optional<Author> findByClerkUserId(String clerkUserId);

    Author save(Author author);
}
