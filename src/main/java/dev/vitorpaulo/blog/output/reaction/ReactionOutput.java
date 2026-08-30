package dev.vitorpaulo.blog.output.reaction;

import dev.vitorpaulo.blog.model.ReactionTargetType;

import java.util.UUID;

public interface ReactionOutput {

    long countByTargetTypeAndTargetId(ReactionTargetType targetType, UUID targetId);
}
