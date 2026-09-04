package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.output.post.PostOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPostBySlugUseCaseTest {

    @Mock private PostOutput postOutput;

    @InjectMocks
    private GetPostBySlugUseCase getPostBySlugUseCase;

    @Test
    void shouldReturnPostFilteredByLanguage() {
        var expected = mock(PostModel.class);
        when(postOutput.findBySlugAndIncrementView("my-post", Language.ENGLISH)).thenReturn(expected);

        var result = getPostBySlugUseCase.execute("my-post", Language.ENGLISH);

        assertEquals(expected, result);
        verify(postOutput).findBySlugAndIncrementView("my-post", Language.ENGLISH);
    }

    @Test
    void shouldPropagateNotFound() {
        when(postOutput.findBySlugAndIncrementView(anyString(), any())).thenThrow(new NotFoundException());

        assertThrows(NotFoundException.class,
                () -> getPostBySlugUseCase.execute("nonexistent", Language.PORTUGUESE));
    }
}
