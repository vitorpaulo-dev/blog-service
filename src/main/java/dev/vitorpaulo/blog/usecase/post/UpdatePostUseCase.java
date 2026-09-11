package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.output.post.PostOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdatePostUseCase {

    private final PostOutput postOutput;

    public PostModel execute(PostModel post, List<UUID> tags, List<UUID> projects, AuthorModel author) {
        return postOutput.update(post, tags, projects, author);
    }
}
