package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.model.ReactionModel;
import dev.vitorpaulo.blog.model.ReactionType;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReactToProjectUseCase {

    private final ProjectOutput projectOutput;

    public ReactionModel execute(String slug, ReactionType reactionType, String ip) {
        return projectOutput.react(slug, reactionType, ip);
    }
}
