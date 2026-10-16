package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.model.PostStatus;
import dev.vitorpaulo.blog.output.post.PostOutput;
import dev.vitorpaulo.blog.output.view.ViewCounterOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPostBySlugUseCaseTest {

    @Mock private PostOutput postOutput;
    @Mock private ViewCounterOutput viewCounterOutput;
    @Mock private PostModel expected;

    @InjectMocks
    private GetPostBySlugUseCase getPostBySlugUseCase;

    @Test
    void execute_published_countsViewOnceAndReturnsModel() {
        var postId = UUID.randomUUID();
        when(postOutput.findBySlug("my-post", Language.ENGLISH)).thenReturn(expected);
        when(expected.status()).thenReturn(PostStatus.PUBLISHED);
        when(expected.id()).thenReturn(postId);

        var result = getPostBySlugUseCase.execute("my-post", Language.ENGLISH, "203.0.113.7");

        assertEquals(expected, result);
        verify(postOutput).findBySlug("my-post", Language.ENGLISH);
        verify(viewCounterOutput).countPostView(postId, "203.0.113.7");
    }

    @Test
    void execute_draft_returnsModelWithoutCountingView() {
        when(postOutput.findBySlug("draft-post", Language.ENGLISH)).thenReturn(expected);
        when(expected.status()).thenReturn(PostStatus.DRAFT);

        var result = getPostBySlugUseCase.execute("draft-post", Language.ENGLISH, "203.0.113.7");

        assertEquals(expected, result);
        verifyNoInteractions(viewCounterOutput);
    }

    @Test
    void execute_notFound_propagatesWithoutCountingView() {
        when(postOutput.findBySlug(anyString(), any()))
                .thenThrow(new NotFoundException(ExceptionCode.POST_SLUG_NOT_FOUND));

        var ex = assertThrows(NotFoundException.class,
                () -> getPostBySlugUseCase.execute("nonexistent", Language.PORTUGUESE, "203.0.113.7"));

        assertEquals(ExceptionCode.POST_SLUG_NOT_FOUND, ex.getCode());
        verifyNoInteractions(viewCounterOutput);
    }
}
