package dev.vitorpaulo.blog.usecase.audio;

import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.audio.AudioModel;
import dev.vitorpaulo.blog.output.audio.AudioOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RetryPostAudioUseCase {

    private final AudioOutput audioOutput;

    public AudioModel execute(UUID postId, AudioType type, Language language, AuthorModel requester) {
        return audioOutput.retry(postId, type, language, requester);
    }
}
