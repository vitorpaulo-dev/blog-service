package dev.vitorpaulo.blog.output.reaction;

import dev.vitorpaulo.blog.model.ReactionTargetType;
import dev.vitorpaulo.blog.output.ReactionOutput;
import dev.vitorpaulo.blog.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReactionOutputAdapter implements ReactionOutput {

    private final ReactionRepository reactionRepository;

    @Override
    public long countByTargetTypeAndTargetId(ReactionTargetType targetType, UUID targetId) {
        return reactionRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }
}
