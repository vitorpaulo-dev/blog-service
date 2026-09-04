package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.input.mapper.PostInputMapper;
import dev.vitorpaulo.blog.input.request.CreatePostRequest;
import dev.vitorpaulo.blog.input.request.MassDeleteRequest;
import dev.vitorpaulo.blog.input.request.PostContentRequest;
import dev.vitorpaulo.blog.input.request.PostQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdatePostRequest;
import dev.vitorpaulo.blog.input.response.PostResponse;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.usecase.post.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock private CreatePostUseCase createPostUseCase;
    @Mock private UpdatePostUseCase updatePostUseCase;
    @Mock private DeletePostUseCase deletePostUseCase;
    @Mock private GetPostByIdUseCase getPostByIdUseCase;
    @Mock private GetPostBySlugUseCase getPostBySlugUseCase;
    @Mock private SearchPostUseCase searchPostUseCase;
    @Mock private PostInputMapper postInputMapper;
    @Mock private AuthorModel author;
    @Mock private PostModel postModel;
    @Mock private PostResponse postResponse;

    @InjectMocks
    private PostController postController;

    @Test
    void create_validRequest_returnsCreatedResponse() {
        var request = mock(CreatePostRequest.class);
        when(request.tagIds()).thenReturn(null);
        when(request.projectIds()).thenReturn(null);
        when(postInputMapper.toModel(request)).thenReturn(postModel);
        when(createPostUseCase.execute(postModel, null, null, author)).thenReturn(postModel);
        when(postInputMapper.toResponse(postModel)).thenReturn(postResponse);

        var result = postController.create(request, author);

        assertEquals(postResponse, result);
        verify(createPostUseCase).execute(postModel, null, null, author);
    }

    @Test
    void update_validRequest_returnsUpdatedResponse() {
        var id = UUID.randomUUID();
        var request = mock(UpdatePostRequest.class);
        when(request.tagIds()).thenReturn(null);
        when(request.projectIds()).thenReturn(null);
        when(postInputMapper.toModel(request, id)).thenReturn(postModel);
        when(updatePostUseCase.execute(postModel, null, null, author)).thenReturn(postModel);
        when(postInputMapper.toResponse(postModel)).thenReturn(postResponse);

        var result = postController.update(id, request, author);

        assertEquals(postResponse, result);
        verify(updatePostUseCase).execute(postModel, null, null, author);
    }

    @Test
    void delete_validRequest_delegatesToUseCase() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        var request = mock(MassDeleteRequest.class);
        when(request.ids()).thenReturn(ids);

        postController.delete(request, author);

        verify(deletePostUseCase).execute(ids, author);
    }

    @Test
    void getById_found_returnsResponse() {
        var id = UUID.randomUUID();
        when(getPostByIdUseCase.execute(id)).thenReturn(postModel);
        when(postInputMapper.toResponse(postModel)).thenReturn(postResponse);

        var result = postController.getById(id);

        assertEquals(postResponse, result);
    }

    @Test
    void getById_notFound_throwsNotFoundException() {
        when(getPostByIdUseCase.execute(any())).thenThrow(new NotFoundException());

        assertThrows(NotFoundException.class, () -> postController.getById(UUID.randomUUID()));
    }

    @Test
    void getBySlug_validSlug_returnsResponse() {
        when(getPostBySlugUseCase.execute("my-post", Language.ENGLISH)).thenReturn(postModel);
        when(postInputMapper.toResponse(postModel)).thenReturn(postResponse);

        var result = postController.getBySlug("my-post", Language.ENGLISH);

        assertEquals(postResponse, result);
        verify(getPostBySlugUseCase).execute("my-post", Language.ENGLISH);
    }

    @Test
    void search_validRequest_returnsPaginatedResponse() {
        var request = mock(GenericPageableRequest.class);
        var paginatedInput = mock(PaginatedInput.class);
        var paginatedOutput = new PaginatedOutput<PostModel>(List.of(), 0, 10, 0, 0);
        var response = mock(GenericPageableResponse.class);

        when(postInputMapper.toPageableInput(request)).thenReturn(paginatedInput);
        when(searchPostUseCase.execute(paginatedInput, author)).thenReturn(paginatedOutput);
        when(postInputMapper.toPageableResponse(paginatedOutput)).thenReturn(response);

        var result = postController.search(request, author);

        assertEquals(response, result);
    }
}
