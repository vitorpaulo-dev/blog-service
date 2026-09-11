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

    @Query("SELECT t FROM TagEntity t JOIN FETCH t.contents c WHERE t.slug = :slug AND c.language = :language")
    Optional<TagEntity> findBySlugAndLanguage(@Param("slug") String slug, @Param("language") Language language);

    long countBySlugAndIdNot(String slug, UUID id);

	@Query("""
		SELECT t
		FROM TagEntity t
		LEFT JOIN FETCH t.contents
		WHERE EXISTS (
			SELECT 1
			FROM t.contents c
			WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%'))
		)
	""")
	Page<TagEntity> search(@Param("name") String name, Pageable pageable);

    @Modifying
    @Query("DELETE FROM TagEntity t WHERE t.id IN :ids")
    void deleteByIdIn(@Param("ids") List<UUID> ids);
}
