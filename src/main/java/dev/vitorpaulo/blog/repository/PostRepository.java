package dev.vitorpaulo.blog.repository;

import java.util.Optional;
import java.util.UUID;

import dev.vitorpaulo.blog.domain.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<PostEntity, UUID> {

    Optional<PostEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT p FROM PostEntity p WHERE " +
            "(:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (:status IS NULL OR p.status = :status)")
    Page<PostEntity> search(@Param("query") String query, @Param("status") dev.vitorpaulo.blog.model.post.PostStatus status, Pageable pageable);

    @Query("SELECT p FROM PostEntity p WHERE " +
            "(:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND p.status = dev.vitorpaulo.blog.model.post.PostStatus.PUBLISHED")
    Page<PostEntity> searchPublished(@Param("query") String query, Pageable pageable);

    Page<PostEntity> findByStatus(dev.vitorpaulo.blog.model.post.PostStatus status, Pageable pageable);

    @Query("SELECT DISTINCT p FROM PostEntity p LEFT JOIN p.authors a WHERE " +
            "(p.status = dev.vitorpaulo.blog.model.post.PostStatus.PUBLISHED OR (p.status = dev.vitorpaulo.blog.model.post.PostStatus.DRAFT AND a.clerkUserId = :clerkUserId)) " +
            "AND (:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<PostEntity> searchVisible(@Param("query") String query, @Param("clerkUserId") String clerkUserId, Pageable pageable);
}
