package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.*;
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
class CreatePostUseCaseTest {

    @Mock private PostOutput postOutput;
    @Mock private PostModel post;
    @Mock private AuthorModel author;
    @Mock private PostModel savedPost;

    @InjectMocks
    private CreatePostUseCase createPostUseCase;

    private UUID tagId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        tagId = UUID.randomUUID();
        projectId = UUID.randomUUID();
    }

    @Test
    void execute_withTagsAndProjects_returnsSavedPost() {
        when(postOutput.save(post, List.of(tagId), List.of(projectId), author)).thenReturn(savedPost);

        var result = createPostUseCase.execute(post, List.of(tagId), List.of(projectId), author);

        assertEquals(savedPost, result);
        verify(postOutput).save(post, List.of(tagId), List.of(projectId), author);
    }

    @Test
    void execute_withoutTagsOrProjects_returnsSavedPost() {
        when(postOutput.save(post, null, null, author)).thenReturn(savedPost);

        var result = createPostUseCase.execute(post, null, null, author);

        assertEquals(savedPost, result);
    }
}
