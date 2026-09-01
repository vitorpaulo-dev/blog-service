package dev.vitorpaulo.blog.repository;

import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.PostStatus;
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

    Optional<PostEntity> findBySlug(String slug);

    long countBySlugAndIdNot(String slug, UUID id);

	@Query(value = """
			SELECT p.*
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
					:query IS NULL
					OR trim(:query) = ''
					OR p.search_vector @@ websearch_to_tsquery('simple', :query)
				)
			ORDER BY
				CASE
					WHEN :query IS NULL OR trim(:query) = ''
					THEN 0
					ELSE ts_rank(p.search_vector, websearch_to_tsquery('simple', :query))
				END DESC,
				p.created_at DESC
		""",
		countQuery = """
			SELECT COUNT(*)
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
					:query IS NULL
					OR trim(:query) = ''
					OR p.search_vector @@ websearch_to_tsquery('simple', :query)
				)
		""",
		nativeQuery = true
	)
	Page<PostEntity> search(
		String query,
		UUID authorId,
		boolean showDrafts,
		Pageable pageable
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
