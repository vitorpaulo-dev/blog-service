package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.output.audio.AudioOutput;
import dev.vitorpaulo.blog.output.post.PostOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetPostBySlugUseCase {

    private final PostOutput postOutput;
    private final AudioOutput audioOutput;

    public PostModel execute(String slug, Language language, String ip) {
        final var post = postOutput.findBySlugAndIncrementView(slug, language, ip);
        return post.withAudio(audioOutput.artifactMap(post.id(), language));
    }
}
