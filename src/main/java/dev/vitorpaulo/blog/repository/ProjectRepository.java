package dev.vitorpaulo.blog.repository;

import java.util.UUID;

import dev.vitorpaulo.blog.domain.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
}
