package dev.vitorpaulo.blog.repository;

import dev.vitorpaulo.blog.domain.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<PostEntity, UUID> {

    @Query("""
        SELECT p FROM PostEntity p
        JOIN p.contents c
        WHERE p.slug = :slug AND c.language = :language
    """)
    Optional<PostEntity> findBySlugAndLanguage(@Param("slug") String slug, @Param("language") dev.vitorpaulo.blog.model.Language language);

    long countBySlugAndIdNot(String slug, UUID id);

    @Query(
        value = """
        SELECT p.*, (p.love_count + p.celebrate_count + p.genius_count + p.help_count) as reactionCount
        FROM post p
        JOIN post_content pc ON pc.post_id = p.id AND pc.language = :language
        WHERE
            (
                p.status = 'PUBLISHED'
                OR (
                    :showDrafts = true
                    AND p.status = 'DRAFT'
                )
            )
            AND (
                :authorId IS NULL
                OR EXISTS (
                    SELECT 1
                    FROM post_author pa
                    WHERE pa.post_id = p.id
                      AND pa.author_id = :authorId
                )
            )
            AND (
                :query IS NULL
                OR pc.search_vector @@ websearch_to_tsquery('simple', :query)
            )
        ORDER BY
            CASE
                WHEN :query IS NULL THEN 0
                ELSE ts_rank(
                    pc.search_vector,
                    websearch_to_tsquery('simple', :query)
                )
            END DESC,
            :sort
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM post p
        JOIN post_content pc ON pc.post_id = p.id AND pc.language = :language
        WHERE
            (
                p.status = 'PUBLISHED'
                OR (
                    :showDrafts = true
                    AND p.status = 'DRAFT'
                )
            )
            AND (
                :authorId IS NULL
                OR EXISTS (
                    SELECT 1
                    FROM post_author pa
                    WHERE pa.post_id = p.id
                      AND pa.author_id = :authorId
                )
            )
            AND (
                :query IS NULL
                OR pc.search_vector @@ websearch_to_tsquery('simple', :query)
            )
        """,
        nativeQuery = true
    )
    Page<PostEntity> search(
        @Param("query") String query,
        @Param("authorId") UUID authorId,
        @Param("language") String language,
        @Param("showDrafts") boolean showDrafts,
        Pageable pageable,
        @Param("sort") String sort
    );

    @Modifying
    @Query("""
        DELETE FROM PostEntity p
        WHERE p.id IN :ids
          AND (
              :bypass = true
              OR EXISTS (
                  SELECT 1
                  FROM p.authors a
                  WHERE a.id = :author
              )
          )
    """)
    void deleteByIdWithAuthor(List<UUID> ids, UUID author, Boolean bypass);

    @Query("""
        SELECT p FROM PostEntity p
        WHERE p.id = :id
          AND (
              :bypass = true
              OR EXISTS (
                  SELECT 1
                  FROM p.authors a
                  WHERE a.id = :author
              )
          )
    """)
    Optional<PostEntity> findByIdWithAuthor(UUID id, UUID author, Boolean bypass);
}
