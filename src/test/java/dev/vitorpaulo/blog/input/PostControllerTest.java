package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.input.mapper.PostInputMapper;
import dev.vitorpaulo.blog.input.request.CreatePostRequest;
import dev.vitorpaulo.blog.input.request.MassDeleteRequest;
import dev.vitorpaulo.blog.input.request.PostQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdatePostRequest;
import dev.vitorpaulo.blog.input.response.PostResponse;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.usecase.post.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    @Mock private CreatePostRequest createRequest;
    @Mock private UpdatePostRequest updateRequest;
    @Mock private MassDeleteRequest massDeleteRequest;
    @Mock private GenericPageableRequest<PostQueryRequest> searchRequest;
    @Mock private PaginatedInput<PostQueryModel> paginatedInput;
    @Mock private PaginatedOutput<PostModel> paginatedOutput;
    @Mock private GenericPageableResponse<PostResponse> pageableResponse;

    @InjectMocks
    private PostController postController;

    @Test
    void create_validRequest_returnsCreatedResponse() {
        when(createRequest.tagIds()).thenReturn(null);
        when(createRequest.projectIds()).thenReturn(null);
        when(postInputMapper.toModel(createRequest)).thenReturn(postModel);
        when(createPostUseCase.execute(postModel, null, null, author)).thenReturn(postModel);
        when(postInputMapper.toResponse(postModel)).thenReturn(postResponse);

        var result = postController.create(createRequest, author);

        assertEquals(postResponse, result);
        verify(createPostUseCase).execute(postModel, null, null, author);
    }

    @Test
    void update_validRequest_returnsUpdatedResponse() {
        var id = UUID.randomUUID();
        when(updateRequest.tagIds()).thenReturn(null);
        when(updateRequest.projectIds()).thenReturn(null);
        when(postInputMapper.toModel(updateRequest, id)).thenReturn(postModel);
        when(updatePostUseCase.execute(postModel, null, null, author)).thenReturn(postModel);
        when(postInputMapper.toResponse(postModel)).thenReturn(postResponse);

        var result = postController.update(id, updateRequest, author);

        assertEquals(postResponse, result);
        verify(updatePostUseCase).execute(postModel, null, null, author);
    }

    @Test
    void delete_validRequest_delegatesToUseCase() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(massDeleteRequest.ids()).thenReturn(ids);

        postController.delete(massDeleteRequest, author);

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
        when(postInputMapper.toPageableInput(searchRequest)).thenReturn(paginatedInput);
        when(searchPostUseCase.execute(paginatedInput, author)).thenReturn(paginatedOutput);
        when(postInputMapper.toPageableResponse(paginatedOutput)).thenReturn(pageableResponse);

        var result = postController.search(searchRequest, author);

        assertEquals(pageableResponse, result);
    }
}
