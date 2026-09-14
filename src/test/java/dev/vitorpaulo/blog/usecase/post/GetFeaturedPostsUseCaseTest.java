package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.output.post.PostOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetFeaturedPostsUseCaseTest {

    @Mock private PostOutput postOutput;
    @Mock private PostModel post;

    @InjectMocks
    private GetFeaturedPostsUseCase getFeaturedPostsUseCase;

    @Test
    void execute_returnsFeaturedPostsFromOutput() {
        when(postOutput.findFeatured(Language.ENGLISH)).thenReturn(List.of(post));

        var result = getFeaturedPostsUseCase.execute(Language.ENGLISH);

        assertEquals(List.of(post), result);
    }
}
