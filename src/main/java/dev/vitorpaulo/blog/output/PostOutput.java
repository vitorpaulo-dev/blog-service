package dev.vitorpaulo.blog.output;

import dev.vitorpaulo.blog.model.post.Post;
import dev.vitorpaulo.blog.model.post.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostOutput {

    Optional<Post> findById(UUID id);

    Optional<Post> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Post save(Post post);

    void delete(UUID id);

    void deleteAll(List<UUID> ids);

    List<Post> findAllById(List<UUID> ids);

    Page<Post> findAll(Pageable pageable);

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    Page<Post> search(String query, PostStatus status, Pageable pageable);

    Page<Post> searchPublished(String query, Pageable pageable);

    Page<Post> searchVisible(String query, String clerkUserId, Pageable pageable);
}
