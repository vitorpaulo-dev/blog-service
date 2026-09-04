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
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePostUseCaseTest {

    @Mock private PostOutput postOutput;
    @Mock private TagOutput tagOutput;
    @Mock private ProjectOutput projectOutput;

    @InjectMocks
    private CreatePostUseCase createPostUseCase;

    private AuthorModel author;
    private PostModel post;

    @BeforeEach
    void setUp() {
        author = new AuthorModel(UUID.randomUUID(), "clerk-1", "Author", "author", null, "org:admin", null);
        post = mock(PostModel.class);
    }

    @Test
    void shouldCreatePostWithTagsAndProjects() {
        var tagId = UUID.randomUUID();
        var projectId = UUID.randomUUID();
        var tag = new TagModel(tagId, "tag-slug", Map.of());
        var project = new ProjectModel(projectId, "proj-slug", null, null, Map.of());
        var savedPost = mock(PostModel.class);

        when(tagOutput.findAllById(List.of(tagId))).thenReturn(List.of(tag));
        when(projectOutput.findAllById(List.of(projectId))).thenReturn(List.of(project));
        when(postOutput.save(post, List.of(tag), List.of(project), author)).thenReturn(savedPost);

        var result = createPostUseCase.execute(post, List.of(tagId), List.of(projectId), author);

        assertEquals(savedPost, result);
        verify(tagOutput).findAllById(List.of(tagId));
        verify(projectOutput).findAllById(List.of(projectId));
        verify(postOutput).save(post, List.of(tag), List.of(project), author);
    }

    @Test
    void shouldCreatePostWithoutTagsOrProjects() {
        var savedPost = mock(PostModel.class);

        when(tagOutput.findAllById(null)).thenReturn(List.of());
        when(projectOutput.findAllById(null)).thenReturn(List.of());
        when(postOutput.save(post, List.of(), List.of(), author)).thenReturn(savedPost);

        var result = createPostUseCase.execute(post, null, null, author);

        assertEquals(savedPost, result);
    }
}
