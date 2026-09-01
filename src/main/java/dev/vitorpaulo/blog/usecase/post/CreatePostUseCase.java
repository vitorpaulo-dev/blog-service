package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.output.post.PostOutput;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import dev.vitorpaulo.blog.output.tag.TagOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreatePostUseCase {

    private final PostOutput postOutput;
    private final TagOutput tagOutput;
    private final ProjectOutput projectOutput;

    public PostModel execute(PostModel post, List<UUID> tagIds, List<UUID> projectIds, AuthorModel author) {
        final var tags = tagOutput.findAllById(tagIds);
        final var projects = projectOutput.findAllById(projectIds);
        return postOutput.save(post, tags, projects, author);
    }
}
