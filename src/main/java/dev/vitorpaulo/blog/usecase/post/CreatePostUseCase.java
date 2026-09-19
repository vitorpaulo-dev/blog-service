package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.output.audio.AudioOutput;
import dev.vitorpaulo.blog.output.post.PostOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreatePostUseCase {

    private final PostOutput postOutput;
    private final AudioOutput audioOutput;

    public PostModel execute(PostModel post, List<UUID> tags, List<UUID> projects, AuthorModel author) {
        final var saved = postOutput.save(post, tags, projects, author);
        audioOutput.dispatchPost(saved.id());
        return saved;
    }
}
