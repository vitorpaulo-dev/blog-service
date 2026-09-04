package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.output.post.PostOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPostByIdUseCaseTest {

    @Mock private PostOutput postOutput;

    @InjectMocks
    private GetPostByIdUseCase getPostByIdUseCase;

    @Test
    void execute_found_returnsPost() {
        var id = UUID.randomUUID();
        var expected = mock(PostModel.class);
        when(postOutput.findById(id)).thenReturn(expected);

        var result = getPostByIdUseCase.execute(id);

        assertEquals(expected, result);
    }

    @Test
    void execute_notFound_throwsNotFoundException() {
        when(postOutput.findById(any())).thenThrow(new NotFoundException());

        assertThrows(NotFoundException.class, () -> getPostByIdUseCase.execute(UUID.randomUUID()));
    }
}
