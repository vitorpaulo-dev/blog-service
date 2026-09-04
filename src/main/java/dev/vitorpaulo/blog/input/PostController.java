package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.config.security.CurrentAuthor;
import dev.vitorpaulo.blog.input.mapper.PostInputMapper;
import dev.vitorpaulo.blog.input.request.CreatePostRequest;
import dev.vitorpaulo.blog.input.request.MassDeleteRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.input.request.PostQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdatePostRequest;
import dev.vitorpaulo.blog.input.response.PostResponse;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.usecase.post.CreatePostUseCase;
import dev.vitorpaulo.blog.usecase.post.DeletePostUseCase;
import dev.vitorpaulo.blog.usecase.post.GetPostByIdUseCase;
import dev.vitorpaulo.blog.usecase.post.GetPostBySlugUseCase;
import dev.vitorpaulo.blog.usecase.post.SearchPostUseCase;
import dev.vitorpaulo.blog.usecase.post.UpdatePostUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/post")
@RequiredArgsConstructor
@Validated
public class PostController {

    private final CreatePostUseCase createPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final GetPostByIdUseCase getPostByIdUseCase;
    private final GetPostBySlugUseCase getPostBySlugUseCase;
    private final SearchPostUseCase searchPostUseCase;

    private final PostInputMapper postInputMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(@Valid @RequestBody CreatePostRequest request, @CurrentAuthor AuthorModel author) {
        final var post = createPostUseCase.execute(postInputMapper.toModel(request), request.tagIds(), request.projectIds(), author);
        return postInputMapper.toResponse(post);
    }

    @PutMapping("/{id}")
    public PostResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePostRequest request, @CurrentAuthor AuthorModel author) {
        final var post = updatePostUseCase.execute(postInputMapper.toModel(request, id), request.tagIds(), request.projectIds(), author);
        return postInputMapper.toResponse(post);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Valid @RequestBody MassDeleteRequest request, @CurrentAuthor AuthorModel author) {
        deletePostUseCase.execute(request.ids(), author);
    }

    @GetMapping("/{id}")
    public PostResponse getById(@PathVariable UUID id) {
        return postInputMapper.toResponse(getPostByIdUseCase.execute(id));
    }

    @GetMapping("/slug/{slug}/{language}")
    public PostResponse getBySlug(@PathVariable String slug, @PathVariable Language language) {
        return postInputMapper.toResponse(getPostBySlugUseCase.execute(slug, language));
    }

    @PostMapping("/search")
    public GenericPageableResponse<PostResponse> search(@Valid @RequestBody GenericPageableRequest<PostQueryRequest> request, @CurrentAuthor AuthorModel author) {
        final var result = searchPostUseCase.execute(postInputMapper.toPageableInput(request), author);
        return postInputMapper.toPageableResponse(result);
    }
}
