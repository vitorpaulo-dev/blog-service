package dev.vitorpaulo.blog.repository;

import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostStatus;
import dev.vitorpaulo.blog.model.TopItemModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<PostEntity, UUID> {

    @Query("SELECT p FROM PostEntity p JOIN p.contents WHERE p.id = :id")
    Optional<PostEntity> findByIdWithContents(UUID id);

    @Query("""
        SELECT DISTINCT p FROM PostEntity p
		JOIN FETCH p.contents c
        WHERE p.slug = :slug
		  AND p.status = 'PUBLISHED'
		  AND c.id = COALESCE(
			  (SELECT pc.id FROM PostContentEntity pc WHERE pc.post = p AND pc.language = :language),
			  (SELECT pc2.id FROM PostContentEntity pc2 WHERE pc2.post = p ORDER BY pc2.language LIMIT 1)
		  )
    """)
    Optional<PostEntity> findBySlugAndLanguage(String slug, Language language);

    Optional<PostEntity> findBySlugAndStatus(String slug, PostStatus status);

    long countBySlugAndIdNot(String slug, UUID id);

	@Query(
		value = """
        SELECT p.*, -1 as reactionCount
        FROM post p
        JOIN post_content pc ON pc.post_id = p.id AND pc.id = COALESCE(
            (SELECT pc2.id FROM post_content pc2 WHERE pc2.post_id = p.id AND pc2.language = :language),
            (SELECT pc3.id FROM post_content pc3 WHERE pc3.post_id = p.id ORDER BY pc3.language LIMIT 1)
        )
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
                :tagId IS NULL
                OR EXISTS (
                    SELECT 1
                    FROM post_tag pt
                    WHERE pt.post_id = p.id
                      AND pt.tag_id = :tagId
                )
            )
            AND (
                :query IS NULL
                OR pc.search_vector @@ websearch_to_tsquery('simple', :query)
            )
        ORDER BY
            CASE WHEN :sort = 'createdAt' AND :direction = 'ASC'
            	THEN p.created_at END ASC,
            CASE WHEN :sort = 'createdAt' AND :direction = 'DESC'
                THEN p.created_at END DESC,
            CASE WHEN :query IS NULL THEN 0
                ELSE ts_rank(
                    pc.search_vector,
                    websearch_to_tsquery('simple', :query)
                )
            END DESC
        """,
		countQuery = """
        SELECT COUNT(1)
        FROM post p
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
                :tagId IS NULL
                OR EXISTS (
                    SELECT 1
                    FROM post_tag pt
                    WHERE pt.post_id = p.id
                      AND pt.tag_id = :tagId
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
		String query,
		UUID authorId,
		UUID tagId,
		String language,
		boolean showDrafts,
		Pageable pageable,
		String sort,
		String direction
	);

    @Query("""
        SELECT DISTINCT p FROM PostEntity p
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
    List<PostEntity> findAllByIdWithAuthor(List<UUID> ids, UUID author, Boolean bypass);

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

	@Query("""
		SELECT t.id FROM PostEntity p
			JOIN p.projects t
			WHERE p.id = :postId
	""")
	List<UUID> findProjectIds(UUID postId);

	@Query("""
		SELECT t.id FROM PostEntity p
			JOIN p.tags t
			WHERE p.id = :postId
	""")
	List<UUID> findTagIds(UUID postId);

    @Query("""
        SELECT p FROM PostEntity p
        JOIN FETCH p.contents
        WHERE p.weight IS NOT NULL
        ORDER BY p.weight ASC
    """)
    List<PostEntity> findFeatured();

    @Modifying
    @Query("""
        UPDATE PostEntity p
        SET p.weight = NULL
        WHERE p.weight IS NOT NULL
    """)
    void clearFeaturedWeights();

    @Modifying
    @Query("""
        UPDATE PostEntity p
        SET p.weight = :weight
        WHERE p.id = :postId
    """)
    void updateWeight(UUID postId, Integer weight);

    long countByStatus(PostStatus status);

    @Query("SELECT COALESCE(SUM(p.viewCount), 0) FROM PostEntity p")
    Long sumViewCount();

    @Query("SELECT COALESCE(SUM(p.loveCount + p.celebrateCount + p.geniusCount + p.helpCount), 0) FROM PostEntity p")
    Long sumReactionCount();

    @Query("""
        SELECT new dev.vitorpaulo.blog.model.TopItemModel(
            p.id,
            COALESCE(
                (SELECT pc.title FROM PostContentEntity pc WHERE pc.post = p AND pc.language = :primary),
                (SELECT pc2.title FROM PostContentEntity pc2 WHERE pc2.post = p ORDER BY pc2.language LIMIT 1)
            ),
            p.slug,
            COALESCE(p.viewCount, 0),
            COALESCE(p.loveCount + p.celebrateCount + p.geniusCount + p.helpCount, 0),
            p.createdAt
        )
        FROM PostEntity p
        ORDER BY p.viewCount DESC
    """)
    List<TopItemModel> findTopByViewCount(Pageable pageable, Language primary);
}
