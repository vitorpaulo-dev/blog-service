package dev.vitorpaulo.blog.repository;

import java.util.UUID;

import dev.vitorpaulo.blog.domain.ReactionEntity;
import dev.vitorpaulo.blog.model.ReactionTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionRepository extends JpaRepository<ReactionEntity, UUID> {
    long countByTargetTypeAndTargetId(ReactionTargetType targetType, UUID targetId);
}
