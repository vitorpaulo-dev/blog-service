package dev.vitorpaulo.blog.repository;

import java.util.UUID;

import dev.vitorpaulo.blog.domain.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<TagEntity, UUID> {
}
