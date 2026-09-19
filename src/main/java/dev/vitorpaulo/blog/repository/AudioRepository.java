package dev.vitorpaulo.blog.repository;

import dev.vitorpaulo.blog.domain.AudioEntity;
import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AudioRepository extends JpaRepository<AudioEntity, UUID> {

    List<AudioEntity> findByPostId(UUID postId);

    Optional<AudioEntity> findByPostIdAndTypeAndLanguage(UUID postId, AudioType type, Language language);
}
