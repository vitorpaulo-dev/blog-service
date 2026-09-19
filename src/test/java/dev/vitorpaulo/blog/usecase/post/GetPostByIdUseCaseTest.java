package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.output.audio.AudioOutput;
import dev.vitorpaulo.blog.output.post.PostOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPostByIdUseCaseTest {

    @Mock private PostOutput postOutput;
    @Mock private AudioOutput audioOutput;
    @Mock private PostModel expected;

    @InjectMocks
    private GetPostByIdUseCase getPostByIdUseCase;

    @Test
    void execute_found_returnsPostWithAudio() {
        var id = UUID.randomUUID();
        when(postOutput.findById(id)).thenReturn(expected);
        doReturn(expected).when(expected).withAudio(any());
        when(audioOutput.artifactMap(id)).thenReturn(Map.of());

        var result = getPostByIdUseCase.execute(id);

        assertEquals(expected, result);
        verify(audioOutput).artifactMap(id);
    }

    @Test
    void execute_notFound_propagatesNotFoundException() {
        when(postOutput.findById(any())).thenThrow(new NotFoundException());

        assertThrows(NotFoundException.class, () -> getPostByIdUseCase.execute(UUID.randomUUID()));
    }
}
