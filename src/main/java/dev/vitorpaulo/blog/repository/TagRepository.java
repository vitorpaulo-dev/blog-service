package dev.vitorpaulo.blog.repository;

import dev.vitorpaulo.blog.domain.TagEntity;
import dev.vitorpaulo.blog.model.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<TagEntity, UUID> {
    @Query("SELECT t FROM TagEntity t JOIN t.contents c WHERE t.slug = :slug AND c.language = :language")
    Optional<TagEntity> findBySlugAndLanguage(String slug, Language language);

    @Query("SELECT t FROM TagEntity t LEFT JOIN t.contents WHERE t.id = :id")
    Optional<TagEntity> findByIdWithContents(UUID id);

	@Query("""
		SELECT DISTINCT t
		FROM TagEntity t
		JOIN t.contents c
		WHERE t.id IN :ids
		ORDER BY t.slug
	""")
	List<TagEntity> findWithSingleContent(List<UUID> ids);

    long countBySlugAndIdNot(String slug, UUID id);

	@Query("""
		SELECT t
		FROM TagEntity t
		JOIN FETCH t.contents c
		WHERE cast(:name as string) IS NULL
			OR EXISTS (
				SELECT tc FROM TagContentEntity tc
				WHERE tc.tag = t
					AND tc.language = :language
					AND lower(tc.name) LIKE lower(concat('%', cast(:name as string), '%'))
			)
	""")
	Page<TagEntity> search(
		String name,
		Language language,
		Pageable pageable
	);

    @Modifying
    @Query("DELETE FROM TagEntity t WHERE t.id IN :ids")
    void deleteByIdIn(List<UUID> ids);
}
