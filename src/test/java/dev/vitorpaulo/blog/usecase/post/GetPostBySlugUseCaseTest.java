package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.output.audio.AudioOutput;
import dev.vitorpaulo.blog.output.post.PostOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPostBySlugUseCaseTest {

    @Mock private PostOutput postOutput;
    @Mock private AudioOutput audioOutput;
    @Mock private PostModel expected;

    @InjectMocks
    private GetPostBySlugUseCase getPostBySlugUseCase;

    @Test
    void execute_validSlug_returnsPostWithUserLanguageAudio() {
        when(postOutput.findBySlugAndIncrementView("my-post", Language.ENGLISH, "203.0.113.7")).thenReturn(expected);
        doReturn(expected).when(expected).withAudio(any());
        when(audioOutput.artifactMap(expected, Language.ENGLISH)).thenReturn(Map.of());

        var result = getPostBySlugUseCase.execute("my-post", Language.ENGLISH, "203.0.113.7");

        assertEquals(expected, result);
        verify(audioOutput).artifactMap(expected, Language.ENGLISH);
    }

    @Test
    void execute_notFound_propagatesNotFoundException() {
        when(postOutput.findBySlugAndIncrementView(anyString(), any(), anyString())).thenThrow(new NotFoundException());

        assertThrows(NotFoundException.class,
                () -> getPostBySlugUseCase.execute("nonexistent", Language.PORTUGUESE, "203.0.113.7"));
    }
}
