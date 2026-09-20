package dev.vitorpaulo.blog.repository;

import dev.vitorpaulo.blog.domain.ProjectEntity;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.ProjectStatus;
import dev.vitorpaulo.blog.model.TopItemModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {

	@Query("SELECT p FROM ProjectEntity p LEFT JOIN p.contents WHERE p.id = :id")
	Optional<ProjectEntity> findByIdWithContents(UUID id);

	@Query("""
		SELECT DISTINCT p FROM ProjectEntity p
		JOIN p.contents c
		WHERE p.id IN :ids
		  AND c.id = COALESCE(
			  (SELECT pc.id FROM ProjectContentEntity pc WHERE pc.project = p AND pc.language = :language),
			  (SELECT pc2.id FROM ProjectContentEntity pc2 WHERE pc2.project = p ORDER BY pc2.language LIMIT 1)
		  )
	""")
	List<ProjectEntity> findAllByIdWithSingleContent(List<UUID> ids, Language language);

	@Query("""
		SELECT p FROM ProjectEntity p
		JOIN FETCH p.contents c
		WHERE p.slug = :slug
		  AND p.status = 'PUBLISHED'
		  AND c.id = COALESCE(
			  (SELECT pc.id FROM ProjectContentEntity pc WHERE pc.project = p AND pc.language = :language),
			  (SELECT pc2.id FROM ProjectContentEntity pc2 WHERE pc2.project = p ORDER BY pc2.language LIMIT 1)
		  )
	""")
	Optional<ProjectEntity> findBySlugAndLanguage(String slug, Language language);

    Optional<ProjectEntity> findBySlugAndStatus(String slug, ProjectStatus status);

    long countBySlugAndIdNot(String slug, UUID id);

    @Query(
        value = """
        SELECT p.*, (p.love_count + p.celebrate_count + p.genius_count + p.help_count) as reactionCount
        FROM project p
        JOIN project_content pc ON pc.project_id = p.id AND pc.id = COALESCE(
            (SELECT pc2.id FROM project_content pc2 WHERE pc2.project_id = p.id AND pc2.language = :language),
            (SELECT pc3.id FROM project_content pc3 WHERE pc3.project_id = p.id ORDER BY pc3.language LIMIT 1)
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
                    FROM project_author pa
                    WHERE pa.project_id = p.id
                      AND pa.author_id = :authorId
                )
            )
            AND (
                :tagId IS NULL
                OR EXISTS (
                    SELECT 1
                    FROM project_tag pt
                    WHERE pt.project_id = p.id
                      AND pt.tag_id = :tagId
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
        FROM project p
        JOIN project_content pc ON pc.project_id = p.id AND pc.id = COALESCE(
            (SELECT pc2.id FROM project_content pc2 WHERE pc2.project_id = p.id AND pc2.language = :language),
            (SELECT pc3.id FROM project_content pc3 WHERE pc3.project_id = p.id ORDER BY pc3.language LIMIT 1)
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
                    FROM project_author pa
                    WHERE pa.project_id = p.id
                      AND pa.author_id = :authorId
                )
            )
            AND (
                :tagId IS NULL
                OR EXISTS (
                    SELECT 1
                    FROM project_tag pt
                    WHERE pt.project_id = p.id
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
    Page<ProjectEntity> search(
        String query,
        UUID authorId,
        UUID tagId,
        String language,
        boolean showDrafts,
        Pageable pageable,
        String sort
    );

    @Query("""
        SELECT DISTINCT p FROM ProjectEntity p
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
    List<ProjectEntity> findAllByIdWithAuthor(List<UUID> ids, UUID author, Boolean bypass);

    @Query("""
        SELECT p FROM ProjectEntity p
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
    Optional<ProjectEntity> findByIdWithAuthor(UUID id, UUID author, Boolean bypass);

    long countByStatus(ProjectStatus status);

    @Query("SELECT COALESCE(SUM(p.viewCount), 0) FROM ProjectEntity p")
    Long sumViewCount();

    @Query("SELECT COALESCE(SUM(p.loveCount + p.celebrateCount + p.geniusCount + p.helpCount), 0) FROM ProjectEntity p")
    Long sumReactionCount();

    @Query("""
        SELECT new dev.vitorpaulo.blog.model.TopItemModel(
            p.id,
            COALESCE(
                (SELECT pc.title FROM ProjectContentEntity pc WHERE pc.project = p AND pc.language = :primary),
                (SELECT pc2.title FROM ProjectContentEntity pc2 WHERE pc2.project = p ORDER BY pc2.language LIMIT 1)
            ),
            p.slug,
            COALESCE(p.viewCount, 0),
            COALESCE(p.loveCount + p.celebrateCount + p.geniusCount + p.helpCount, 0),
            p.createdAt
        )
        FROM ProjectEntity p
        ORDER BY p.viewCount DESC
    """)
    List<TopItemModel> findTopByViewCount(Pageable pageable, Language primary);
}
