package dev.vitorpaulo.blog.usecase.audio;

import dev.vitorpaulo.blog.model.AudioStatus;
import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.audio.AudioModel;
import dev.vitorpaulo.blog.output.audio.AudioOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetryPostAudioUseCaseTest {

    @Mock private AudioOutput audioOutput;

    @InjectMocks
    private RetryPostAudioUseCase retryPostAudioUseCase;

    @Test
    void execute_delegatesToOutputWithRequester() {
        var postId = UUID.randomUUID();
        var requester = new AuthorModel(null, "user_1", null, null, null, "org:member");
        var model = new AudioModel(AudioType.PODCAST, Language.PORTUGUESE, AudioStatus.QUEUED, "post/audio/1/k.wav", null, null);
        when(audioOutput.retry(postId, AudioType.PODCAST, Language.PORTUGUESE, requester)).thenReturn(model);

        var result = retryPostAudioUseCase.execute(postId, AudioType.PODCAST, Language.PORTUGUESE, requester);

        assertEquals(model, result);
        verify(audioOutput).retry(postId, AudioType.PODCAST, Language.PORTUGUESE, requester);
    }
}
