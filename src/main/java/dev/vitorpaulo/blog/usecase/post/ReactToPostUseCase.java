package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.ReactionModel;
import dev.vitorpaulo.blog.model.ReactionType;
import dev.vitorpaulo.blog.output.post.PostOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReactToPostUseCase {

    private final PostOutput postOutput;

    public ReactionModel execute(String slug, ReactionType reactionType, String ip) {
        return postOutput.react(slug, reactionType, ip);
    }
}
