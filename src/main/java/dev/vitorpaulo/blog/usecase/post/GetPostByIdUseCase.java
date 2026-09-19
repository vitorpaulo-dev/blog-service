package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.output.audio.AudioOutput;
import dev.vitorpaulo.blog.output.post.PostOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetPostByIdUseCase {

    private final PostOutput postOutput;
    private final AudioOutput audioOutput;

    public PostModel execute(UUID id) {
        final var post = postOutput.findById(id);
        return post.withAudio(audioOutput.artifactMap(post));
    }
}
