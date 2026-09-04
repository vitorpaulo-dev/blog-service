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
import org.junit.jupiter.api.Nested;
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

    @InjectMocks
    private PostController postController;

    private AuthorModel author;

    @BeforeEach
    void setUp() {
        author = new AuthorModel(UUID.randomUUID(), "clerk-1", "Author", "author", null, "org:admin", null);
    }

    @Nested
    class Create {

        @Test
        void shouldCreatePostAndReturnResponse() {
            var request = new CreatePostRequest(
                    "banner.jpg",
                    Map.of(Language.ENGLISH, new PostContentRequest("Title", "Content")),
                    null, null, "DRAFT"
            );
            var postModel = mock(PostModel.class);
            var response = mock(PostResponse.class);

            when(postInputMapper.toModel(request)).thenReturn(postModel);
            when(createPostUseCase.execute(postModel, null, null, author)).thenReturn(postModel);
            when(postInputMapper.toResponse(postModel)).thenReturn(response);

            var result = postController.create(request, author);

            assertEquals(response, result);
            verify(createPostUseCase).execute(postModel, null, null, author);
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdatePostAndReturnResponse() {
            var id = UUID.randomUUID();
            var request = new UpdatePostRequest(
                    "banner.jpg",
                    Map.of(Language.ENGLISH, new PostContentRequest("Title", "Content")),
                    null, null, PostStatus.PUBLISHED
            );
            var postModel = mock(PostModel.class);
            var response = mock(PostResponse.class);

            when(postInputMapper.toModel(request, id)).thenReturn(postModel);
            when(updatePostUseCase.execute(postModel, null, null, author)).thenReturn(postModel);
            when(postInputMapper.toResponse(postModel)).thenReturn(response);

            var result = postController.update(id, request, author);

            assertEquals(response, result);
            verify(updatePostUseCase).execute(postModel, null, null, author);
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldDeletePosts() {
            var ids = List.of(UUID.randomUUID(), UUID.randomUUID());
            var request = new MassDeleteRequest(ids);

            postController.delete(request, author);

            verify(deletePostUseCase).execute(ids, author);
        }
    }

    @Nested
    class GetById {

        @Test
        void shouldReturnPostResponse() {
            var id = UUID.randomUUID();
            var postModel = mock(PostModel.class);
            var response = mock(PostResponse.class);

            when(getPostByIdUseCase.execute(id)).thenReturn(postModel);
            when(postInputMapper.toResponse(postModel)).thenReturn(response);

            var result = postController.getById(id);

            assertEquals(response, result);
        }

        @Test
        void shouldPropagateNotFound() {
            when(getPostByIdUseCase.execute(any())).thenThrow(new NotFoundException());

            assertThrows(NotFoundException.class, () -> postController.getById(UUID.randomUUID()));
        }
    }

    @Nested
    class GetBySlug {

        @Test
        void shouldReturnPostForLanguage() {
            var postModel = mock(PostModel.class);
            var response = mock(PostResponse.class);

            when(getPostBySlugUseCase.execute("my-post", Language.ENGLISH)).thenReturn(postModel);
            when(postInputMapper.toResponse(postModel)).thenReturn(response);

            var result = postController.getBySlug("my-post", Language.ENGLISH);

            assertEquals(response, result);
            verify(getPostBySlugUseCase).execute("my-post", Language.ENGLISH);
        }
    }

    @Nested
    class Search {

        @Test
        void shouldReturnPaginatedResults() {
            var queryRequest = new PostQueryRequest("test", null, Language.ENGLISH);
            var request = new GenericPageableRequest<>(queryRequest, 0, 10, "createdAt", Sort.Direction.DESC);
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
}
