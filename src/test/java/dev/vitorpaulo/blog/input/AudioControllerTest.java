package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.input.mapper.AudioInputMapper;
import dev.vitorpaulo.blog.input.response.AudioResponse;
import dev.vitorpaulo.blog.model.AudioStatus;
import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.audio.AudioModel;
import dev.vitorpaulo.blog.usecase.audio.RetryPostAudioUseCase;
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
class AudioControllerTest {

    @Mock private RetryPostAudioUseCase retryPostAudioUseCase;
    @Mock private AudioInputMapper audioInputMapper;

    @InjectMocks
    private AudioController audioController;

    @Test
    void retry_returnsUpdatedResponse() {
        var postId = UUID.randomUUID();
        var model = new AudioModel(AudioType.NARRATION, Language.ENGLISH, AudioStatus.QUEUED, "post/audio/1/k.wav", null, null);
        var response = new AudioResponse(AudioType.NARRATION, Language.ENGLISH, AudioStatus.QUEUED, "post/audio/1/k.wav", null, null);
        when(retryPostAudioUseCase.execute(postId, AudioType.NARRATION, Language.ENGLISH)).thenReturn(model);
        when(audioInputMapper.toResponse(model)).thenReturn(response);

        var result = audioController.retry(postId, AudioType.NARRATION, Language.ENGLISH);

        assertEquals(response, result);
        verify(retryPostAudioUseCase).execute(postId, AudioType.NARRATION, Language.ENGLISH);
    }
}
