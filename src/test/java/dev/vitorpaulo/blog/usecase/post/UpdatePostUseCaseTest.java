package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.output.post.PostOutput;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import dev.vitorpaulo.blog.output.tag.TagOutput;
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
    @Mock private TagOutput tagOutput;
    @Mock private ProjectOutput projectOutput;
    @Mock private PostModel post;
    @Mock private AuthorModel author;
    @Mock private TagModel tag;
    @Mock private ProjectModel project;

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
        var updatedPost = mock(PostModel.class);

        when(tagOutput.findAllById(List.of(tagId))).thenReturn(List.of(tag));
        when(projectOutput.findAllById(List.of(projectId))).thenReturn(List.of(project));
        when(postOutput.update(post, List.of(tag), List.of(project), author)).thenReturn(updatedPost);

        var result = updatePostUseCase.execute(post, List.of(tagId), List.of(projectId), author);

        assertEquals(updatedPost, result);
        verify(postOutput).update(post, List.of(tag), List.of(project), author);
    }

    @Test
    void execute_withoutTagsOrProjects_returnsUpdatedPost() {
        var updatedPost = mock(PostModel.class);

        when(tagOutput.findAllById(null)).thenReturn(List.of());
        when(projectOutput.findAllById(null)).thenReturn(List.of());
        when(postOutput.update(post, List.of(), List.of(), author)).thenReturn(updatedPost);

        var result = updatePostUseCase.execute(post, null, null, author);

        assertEquals(updatedPost, result);
    }
}
