package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.output.audio.AudioOutput;
import dev.vitorpaulo.blog.output.post.PostOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePostUseCaseTest {

    @Mock private PostOutput postOutput;
    @Mock private AudioOutput audioOutput;
    @Mock private PostModel post;
    @Mock private AuthorModel author;
    @Mock private PostModel updatedPost;

    @InjectMocks
    private UpdatePostUseCase updatePostUseCase;

    private UUID tagId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        tagId = UUID.randomUUID();
        projectId = UUID.randomUUID();
    }

    @Test
    void execute_withTagsAndProjects_returnsUpdatedPost() {
        when(postOutput.update(post, List.of(tagId), List.of(projectId), author)).thenReturn(updatedPost);
        when(updatedPost.id()).thenReturn(UUID.randomUUID());
        when(updatedPost.id()).thenReturn(UUID.randomUUID());

        var result = updatePostUseCase.execute(post, List.of(tagId), List.of(projectId), author);

        assertEquals(updatedPost, result);
        verify(postOutput).update(post, List.of(tagId), List.of(projectId), author);
        verify(audioOutput).dispatchPost(updatedPost.id());
    }

    @Test
    void execute_withoutTagsOrProjects_returnsUpdatedPost() {
        when(postOutput.update(post, null, null, author)).thenReturn(updatedPost);
        when(updatedPost.id()).thenReturn(UUID.randomUUID());

        var result = updatePostUseCase.execute(post, null, null, author);

        assertEquals(updatedPost, result);
        verify(audioOutput).dispatchPost(updatedPost.id());
    }
}
