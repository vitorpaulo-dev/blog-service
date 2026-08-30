package dev.vitorpaulo.blog.repository;

import java.util.Optional;
import java.util.UUID;

import dev.vitorpaulo.blog.domain.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<AuthorEntity, UUID> {
    Optional<AuthorEntity> findByClerkUserId(String clerkUserId);
}
