package dev.vitorpaulo.blog.repository;

import java.util.UUID;

import dev.vitorpaulo.blog.domain.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<AuthorEntity, UUID> {
    java.util.Optional<AuthorEntity> findBySubjectId(String subjectId);
    long countBySlugAndIdNot(String slug, UUID id);
}
