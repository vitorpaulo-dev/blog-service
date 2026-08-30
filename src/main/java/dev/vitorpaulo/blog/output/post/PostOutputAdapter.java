package dev.vitorpaulo.blog.output.post;

import dev.vitorpaulo.blog.mapper.PostMapper;
import dev.vitorpaulo.blog.model.post.Post;
import dev.vitorpaulo.blog.model.post.PostStatus;
import dev.vitorpaulo.blog.output.PostOutput;
import dev.vitorpaulo.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostOutputAdapter implements PostOutput {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    @Override
    public Optional<Post> findById(UUID id) {
        return postRepository.findById(id).map(postMapper::toModel);
    }

    @Override
    public Optional<Post> findBySlug(String slug) {
        return postRepository.findBySlug(slug).map(postMapper::toModel);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return postRepository.existsBySlug(slug);
    }

    @Override
    public Post save(Post post) {
        var entity = postMapper.toEntity(post);
        var saved = postRepository.save(entity);
        return postMapper.toModel(saved);
    }

    @Override
    public void delete(UUID id) {
        postRepository.deleteById(id);
    }

    @Override
    public void deleteAll(List<UUID> ids) {
        postRepository.deleteAllById(ids);
    }

    @Override
    public List<Post> findAllById(List<UUID> ids) {
        return postRepository.findAllById(ids).stream().map(postMapper::toModel).toList();
    }

    @Override
    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAll(pageable).map(postMapper::toModel);
    }

    @Override
    public Page<Post> findByStatus(PostStatus status, Pageable pageable) {
        return postRepository.findByStatus(status, pageable).map(postMapper::toModel);
    }

    @Override
    public Page<Post> search(String query, PostStatus status, Pageable pageable) {
        return postRepository.search(query, status, pageable).map(postMapper::toModel);
    }

    @Override
    public Page<Post> searchPublished(String query, Pageable pageable) {
        return postRepository.searchPublished(query, pageable).map(postMapper::toModel);
    }

    @Override
    public Page<Post> searchVisible(String query, String clerkUserId, Pageable pageable) {
        return postRepository.searchVisible(query, clerkUserId, pageable).map(postMapper::toModel);
    }
}
